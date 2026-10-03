package com.xdev.ooms.production.dayimport.service;

import com.xdev.ooms.production.dayimport.dto.DayImportDriveStatusDto;
import com.xdev.ooms.production.dayimport.dto.DayImportReportDto;
import com.xdev.ooms.production.dayimport.entity.TenantGoogleDriveCredential;
import com.xdev.ooms.production.dayimport.repository.TenantGoogleDriveCredentialRepository;
import com.xdev.ooms.production.parameter.entity.Parameter;
import com.xdev.ooms.production.parameter.service.BooleanParameterReader;
import com.xdev.ooms.production.parameter.service.ParameterService;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Google Drive day-import sync for the current tenant's connected Google account.
 */
@Service
public class DayImportDriveService {

    private final BooleanParameterReader booleanParameterReader;
    private final ParameterService parameterService;
    private final DayImportDriveClient driveClient;
    private final DayImportWorkflow workflow;
    private final DayImportDriveStore store;
    @org.springframework.beans.factory.annotation.Value("${oosm.import.gdrive.cron:0 0 6 * * *}")
    private String cron;
    private final GoogleDriveOAuthService oauthService;
    private final TenantGoogleDriveCredentialRepository credentialRepository;

    public DayImportDriveService(
            BooleanParameterReader booleanParameterReader,
            ParameterService parameterService,
            DayImportDriveClient driveClient,
            DayImportWorkflow workflow,
            DayImportDriveStore store,
            GoogleDriveOAuthService oauthService,
            TenantGoogleDriveCredentialRepository credentialRepository) {
        this.booleanParameterReader = booleanParameterReader;
        this.parameterService = parameterService;
        this.driveClient = driveClient;
        this.workflow = workflow;
        this.store = store;
        this.oauthService = oauthService;
        this.credentialRepository = credentialRepository;
    }

    public DayImportDriveStatusDto status() {
        UUID tenantId = DayImportAccess.tenant();
        DayImportDriveStatusDto dto = store.status();
        boolean enabled = booleanParameterReader.isEnabled("IMPORT_GDRIVE_ENABLED", false);
        String folderId = readParam("IMPORT_GDRIVE_FOLDER_ID");
        String processedFolderId = readParam("IMPORT_GDRIVE_PROCESSED_FOLDER_ID");
        boolean oauthConfigured = oauthService.isOAuthConfigured();
        boolean connected = oauthService.isConnected(tenantId);
        String email = oauthService.findActive(tenantId).map(TenantGoogleDriveCredential::getGoogleAccountEmail).orElse(null);

        dto.setEnabled(enabled);
        dto.setFolderId(folderId);
        dto.setProcessedFolderId(processedFolderId);
        dto.setOauthConfigured(oauthConfigured);
        dto.setConnected(connected);
        dto.setGoogleAccountEmail(email);
        dto.setConfigured(enabled && folderId != null && !folderId.isBlank() && oauthConfigured && connected);
        dto.setCron(cron);
        return dto;
    }

    public Map<String, String> beginAuthorize() {
        DayImportAccess.requireAdmin();
        UUID tenantId = DayImportAccess.tenant();
        OOSMLogger.logMethodEntry(getClass(), "beginAuthorize", tenantId);
        String url = oauthService.buildAuthorizeUrl(tenantId);
        Map<String, String> payload = new HashMap<>();
        payload.put("authorizeUrl", url);
        OOSMLogger.logBusinessEvent(getClass(), "GDRIVE_OAUTH_AUTHORIZE_START", "tenantId=" + tenantId);
        return payload;
    }

    public DayImportDriveStatusDto disconnect() {
        DayImportAccess.requireAdmin();
        UUID tenantId = DayImportAccess.tenant();
        OOSMLogger.logMethodEntry(getClass(), "disconnect", tenantId);
        oauthService.disconnect(tenantId);

        OOSMLogger.logBusinessEvent(getClass(), "GDRIVE_OAUTH_DISCONNECT", "tenantId=" + tenantId);
        return status();
    }

    public DayImportDriveStatusDto syncNow() {
        DayImportAccess.requireImport();
        DayImportDriveStatusDto result = status();
        if (!result.isConfigured() || result.getProcessedFolderId() == null || result.getProcessedFolderId().isBlank()) {
            result.setLastResult("SKIPPED_NOT_CONFIGURED");
            result.setLastError("Connect and enable Drive, then configure distinct source and processed folders");
            return result;
        }
        if (result.getFolderId().equals(result.getProcessedFolderId())) throw new IllegalArgumentException("Source and processed folders must differ");
        UUID lease = store.acquire();
        if (lease == null) { result.setLastResult("RUNNING"); return result; }
        int processed=0, failed=0, pendingMoves=0;
        result.setLastSyncAt(Instant.now());
        result.setLastError(null);
        java.util.Set<String> routingFiles = new java.util.HashSet<>();
        try {
            // Routing retry never invokes the import engine, even after a process restart.
            for (var pending : store.pending()) {
                store.renew(lease);
                if (!status().isConfigured()) throw new IllegalStateException("Drive disconnected or disabled during sync");
                routingFiles.add(pending.fileId());
                try {
                    if (!DayImportLedger.digest(driveClient.download(pending.fileId())).equals(pending.digest()))
                        throw new IllegalStateException("Committed file changed before routing; reconcile manually");
                    driveClient.moveToFolder(pending.fileId(), pending.folder());
                    store.routed(pending.fileId());
                } catch (Exception e) { pendingMoves++; result.setLastError("Committed file routing pending: " + pending.fileId()); }
            }
            var files = driveClient.listXlsx(result.getFolderId());
            result.setPendingCount(files.size());
            for (var file : files) {
                if (routingFiles.contains(file.id())) continue;
                store.renew(lease);
                if (!status().isConfigured()) throw new IllegalStateException("Drive disconnected or disabled during sync");
                boolean committed=false;
                try {
                    byte[] bytes=driveClient.download(file.id());
                    if (bytes.length > 20 * 1024 * 1024) throw new IllegalArgumentException("Workbook exceeds 20 MB");
                    var preview=workflow.preview(bytes, "DRIVE");
                    if (!preview.isCanCommit()) { failed++; result.setLastError("Validation failed: " + file.name() + "; run=" + preview.getRunId()); continue; }
                    var report=workflow.commit(bytes, preview.getRunId());
                    if (!java.util.Set.of("COMMITTED", "REPLAYED").contains(report.getOutcome())) throw new IllegalStateException("Import did not commit");
                    committed=true;
                    processed++;
                    store.routing(file.id(), DayImportLedger.digest(bytes), result.getProcessedFolderId());
                    if (!DayImportLedger.digest(driveClient.download(file.id())).equals(DayImportLedger.digest(bytes)))
                        throw new IllegalStateException("Committed workbook changed before routing");
                    driveClient.moveToFolder(file.id(), result.getProcessedFolderId());
                    store.routed(file.id());
                } catch (Exception e) {
                    if (committed) { pendingMoves++; result.setLastError("Committed; file routing pending: " + file.name()); }
                    else { failed++; result.setLastError("Import failed: " + file.name()); }
                    OOSMLogger.logException(getClass(), "Drive file processing failed", e);
                }
            }
            result.setLastResult(pendingMoves > 0 ? "COMMITTED_ROUTING_PENDING" : failed > 0 ? "COMPLETED_WITH_ERRORS" : "OK");
        } catch (Exception e) {
            result.setLastResult("ERROR"); result.setLastError("Drive sync interrupted; committed files remain recorded");
            OOSMLogger.logException(getClass(), "Drive sync failed", e);
        } finally {
            result.setProcessedCount(processed); result.setFailedCount(failed);
            result.setPendingCount(pendingMoves + failed);
            store.finish(lease, result);
        }
        return result;
    }

    @Scheduled(cron = "${oosm.import.gdrive.cron:0 0 6 * * *}")
    public void scheduledSync() {
        OOSMLogger.info(getClass(), "[scheduledSync] starting");
        for (TenantGoogleDriveCredential cred : credentialRepository.findAllByIsDeletedFalse()) {
            if (cred.getTenantId() == null) {
                continue;
            }
            try {
                TenantContext.setCurrentTenant(cred.getTenantId());
                if (!booleanParameterReader.isEnabled("IMPORT_GDRIVE_ENABLED", false)) {
                    OOSMLogger.debug(getClass(), "[scheduledSync] skip disabled tenant={}", cred.getTenantId());
                    continue;
                }
                OOSMLogger.info(getClass(), "[scheduledSync] tenant={}", cred.getTenantId());
                DayImportAccess.automated(this::syncNow);
            } catch (Exception e) {
                OOSMLogger.logException(getClass(),
                        "[scheduledSync] tenant failed id=" + cred.getTenantId(), e);
            } finally {
                TenantContext.clear();
            }
        }
        OOSMLogger.info(getClass(), "[scheduledSync] finished");
    }

    private String readParam(String code) {
        try {
            UUID tenantId = DayImportAccess.tenant();
            if (tenantId == null) {
                return "";
            }
            Parameter parameter = parameterService.getByCode(code, tenantId);
            return parameter != null && parameter.getValue() != null ? parameter.getValue() : "";
        } catch (Exception e) {
            return "";
        }
    }
}
