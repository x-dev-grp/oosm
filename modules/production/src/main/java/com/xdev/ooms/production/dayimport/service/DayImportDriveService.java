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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Google Drive day-import sync for the current tenant's connected Google account.
 */
@Service
public class DayImportDriveService {

    private final BooleanParameterReader booleanParameterReader;
    private final ParameterService parameterService;
    private final DayImportDriveClient driveClient;
    private final DayImportService dayImportService;
    private final GoogleDriveOAuthService oauthService;
    private final TenantGoogleDriveCredentialRepository credentialRepository;

    private final AtomicReference<Instant> lastSyncAt = new AtomicReference<>();
    private final AtomicReference<String> lastResult = new AtomicReference<>("NEVER");
    private final AtomicReference<String> lastError = new AtomicReference<>();
    private final AtomicInteger pendingCount = new AtomicInteger();
    private final AtomicInteger processedCount = new AtomicInteger();
    private final AtomicInteger failedCount = new AtomicInteger();

    public DayImportDriveService(
            BooleanParameterReader booleanParameterReader,
            ParameterService parameterService,
            DayImportDriveClient driveClient,
            DayImportService dayImportService,
            GoogleDriveOAuthService oauthService,
            TenantGoogleDriveCredentialRepository credentialRepository) {
        this.booleanParameterReader = booleanParameterReader;
        this.parameterService = parameterService;
        this.driveClient = driveClient;
        this.dayImportService = dayImportService;
        this.oauthService = oauthService;
        this.credentialRepository = credentialRepository;
    }

    public DayImportDriveStatusDto status() {
        UUID tenantId = TenantContext.getCurrentTenant();
        DayImportDriveStatusDto dto = new DayImportDriveStatusDto();
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
        dto.setCron(readParam("IMPORT_GDRIVE_CRON"));
        dto.setLastSyncAt(lastSyncAt.get());
        dto.setLastResult(lastResult.get());
        dto.setLastError(lastError.get());
        dto.setPendingCount(pendingCount.get());
        dto.setProcessedCount(processedCount.get());
        dto.setFailedCount(failedCount.get());
        return dto;
    }

    public Map<String, String> beginAuthorize() {
        UUID tenantId = TenantContext.getCurrentTenant();
        OOSMLogger.logMethodEntry(getClass(), "beginAuthorize", tenantId);
        String url = oauthService.buildAuthorizeUrl(tenantId);
        Map<String, String> payload = new HashMap<>();
        payload.put("authorizeUrl", url);
        OOSMLogger.logBusinessEvent(getClass(), "GDRIVE_OAUTH_AUTHORIZE_START", "tenantId=" + tenantId);
        return payload;
    }

    public DayImportDriveStatusDto disconnect() {
        UUID tenantId = TenantContext.getCurrentTenant();
        OOSMLogger.logMethodEntry(getClass(), "disconnect", tenantId);
        oauthService.disconnect(tenantId);
        lastResult.set("DISCONNECTED");
        lastError.set(null);
        OOSMLogger.logBusinessEvent(getClass(), "GDRIVE_OAUTH_DISCONNECT", "tenantId=" + tenantId);
        return status();
    }

    public DayImportDriveStatusDto syncNow() {
        long start = System.currentTimeMillis();
        UUID tenantId = TenantContext.getCurrentTenant();
        OOSMLogger.logMethodEntry(getClass(), "syncNow", tenantId);
        lastSyncAt.set(Instant.now());
        DayImportDriveStatusDto before = status();
        if (!before.isOauthConfigured()) {
            lastResult.set("SKIPPED_OAUTH_NOT_CONFIGURED");
            lastError.set("Set oosm.import.gdrive.oauth.client-id/secret/redirect-uri on the server");
            OOSMLogger.warn(getClass(), "[syncNow] skipped oauth not configured tenant={}", tenantId);
            return status();
        }
        if (!before.isConnected()) {
            lastResult.set("SKIPPED_NOT_CONNECTED");
            lastError.set("Connect Google Drive with the Connect button (personal or Workspace account)");
            OOSMLogger.warn(getClass(), "[syncNow] skipped not connected tenant={}", tenantId);
            return status();
        }
        if (!before.isEnabled()) {
            lastResult.set("SKIPPED_DISABLED");
            lastError.set("Enable IMPORT_GDRIVE_ENABLED for this tenant");
            OOSMLogger.warn(getClass(), "[syncNow] skipped disabled tenant={}", tenantId);
            return status();
        }
        if (before.getFolderId() == null || before.getFolderId().isBlank()) {
            lastResult.set("SKIPPED_NO_FOLDER");
            lastError.set("Set IMPORT_GDRIVE_FOLDER_ID (Drive folder shared with the connected account)");
            OOSMLogger.warn(getClass(), "[syncNow] skipped no folder tenant={}", tenantId);
            return status();
        }

        String folderId = before.getFolderId();
        String processedFolderId = before.getProcessedFolderId();
        String failedFolderId = readParam("IMPORT_GDRIVE_FAILED_FOLDER_ID");
        int ok = 0;
        int fail = 0;
        try {
            var files = driveClient.listXlsx(folderId);
            pendingCount.set(files.size());
            OOSMLogger.info(getClass(), "[syncNow] tenant={} folder={} pendingFiles={}",
                    tenantId, folderId, files.size());
            for (DayImportDriveClient.DriveFileRef file : files) {
                try {
                    byte[] bytes = driveClient.download(file.id());
                    DayImportReportDto dry = dayImportService.dryRun(bytes);
                    if (!dry.isCanCommit()) {
                        fail++;
                        if (failedFolderId != null && !failedFolderId.isBlank()) {
                            driveClient.moveToFolder(file.id(), failedFolderId);
                        }
                        lastError.set("Dry-run failed for " + file.name() + " (" + dry.getInvalidCount() + " errors)");
                        OOSMLogger.warn(getClass(), "[syncNow] dry-run failed file={} invalid={}",
                                file.name(), dry.getInvalidCount());
                        continue;
                    }
                    dayImportService.commit(bytes);
                    if (processedFolderId != null && !processedFolderId.isBlank()) {
                        driveClient.moveToFolder(file.id(), processedFolderId);
                    }
                    ok++;
                    OOSMLogger.info(getClass(), "[syncNow] committed file={}", file.name());
                } catch (Exception ex) {
                    fail++;
                    lastError.set(file.name() + ": " + ex.getMessage());
                    OOSMLogger.logException(getClass(), "[syncNow] file failed name=" + file.name(), ex);
                    if (failedFolderId != null && !failedFolderId.isBlank()) {
                        try {
                            driveClient.moveToFolder(file.id(), failedFolderId);
                        } catch (Exception moveEx) {
                            OOSMLogger.logException(getClass(), "[syncNow] move to failed folder failed", moveEx);
                        }
                    }
                }
            }
            processedCount.addAndGet(ok);
            failedCount.addAndGet(fail);
            lastResult.set("OK processed=" + ok + " failed=" + fail);
            if (fail == 0) {
                lastError.set(null);
            }
            OOSMLogger.logBusinessEvent(getClass(), "DAY_IMPORT_DRIVE_SYNC",
                    "tenant=" + tenantId + " processed=" + ok + " failed=" + fail);
        } catch (Exception e) {
            lastResult.set("ERROR");
            lastError.set(e.getMessage());
            OOSMLogger.logException(getClass(), "[syncNow] failed tenant=" + tenantId, e);
        }
        OOSMLogger.logPerformance(getClass(), "syncNow", start, System.currentTimeMillis());
        return status();
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
                syncNow();
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
            UUID tenantId = TenantContext.getCurrentTenant();
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
