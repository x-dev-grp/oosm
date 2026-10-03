package com.xdev.ooms.production.dayimport.service;

import com.xdev.ooms.production.dayimport.dto.DayImportReportDto;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.UUID;

/** Nontransactional orchestration preserves rejected-run evidence after the business rollback. */
@Service
public class DayImportWorkflow {
    private final DayImportService engine;
    private final DayImportLedger ledger;
    private final TransactionTemplate transaction;
    public DayImportWorkflow(DayImportService engine, DayImportLedger ledger, PlatformTransactionManager manager) {
        this.engine = engine; this.ledger = ledger; this.transaction = new TransactionTemplate(manager);
    }

    public DayImportReportDto preview(byte[] bytes, String source) throws Exception {
        DayImportAccess.requireImport();
        DayImportReportDto report = engine.dryRun(bytes);
        report.setRunId(UUID.randomUUID());
        report.setOutcome("PREVIEW");
        var auth = SecurityContextHolder.getContext().getAuthentication();
        ledger.createRun(report.getRunId(), DayImportLedger.digest(bytes), source, auth == null ? "AUTOMATION" : auth.getName(), report);
        return report;
    }

    public DayImportReportDto commit(byte[] bytes, UUID previewId) throws Exception {
        DayImportAccess.requireImport();
        var preview = ledger.run(previewId);
        String digest = DayImportLedger.digest(bytes);
        if (!digest.equals(preview.digest())) throw new DayImportRejectedException("Selected file differs from preview", preview.report());
        try {
            return transaction.execute(status -> {
                ledger.lockTenant();
                // Recheck domain permissions even when returning a previously committed result.
                try {
                    engine.authorize(bytes);
                    DayImportReportDto existing = ledger.committed(digest);
                    if (existing != null) {
                        if (!previewId.equals(existing.getRunId())) {
                            existing.setRunId(previewId); existing.setOutcome("REPLAYED"); ledger.finish(previewId, "REPLAYED", existing);
                        } else existing.setOutcome("REPLAYED");
                        return existing;
                    }
                    if (!"PREVIEW".equals(preview.outcome()) || preview.createdAt().isBefore(Instant.now().minusSeconds(3600)))
                        throw new DayImportRejectedException("Preview expired; validate again", preview.report());
                    DayImportReportDto current = engine.dryRun(bytes);
                    if (!current.isCanCommit() || !ledger.effects(current).equals(ledger.effects(preview.report())))
                        throw new DayImportRejectedException("Import conditions changed; validate again", current);
                    DayImportReportDto report = engine.commit(bytes);
                    report.setRunId(previewId);
                    report.setOutcome("COMMITTED");
                    ledger.finish(previewId, "COMMITTED", report);
                    return report;
                } catch (RuntimeException e) { throw e; }
                catch (Exception e) { throw new IllegalStateException("Import failed", e); }
            });
        } catch (org.springframework.security.access.AccessDeniedException e) { throw e; }
        catch (RuntimeException e) {
            DayImportReportDto report = e instanceof DayImportRejectedException rejected ? rejected.getReport() : preview.report();
            report.setRunId(previewId); report.setOutcome("REJECTED"); report.setCanCommit(false);
            // The transaction has ended. Do not replace a successful record during a concurrent retry.
            if (!"COMMITTED".equals(ledger.run(previewId).outcome())) ledger.finish(previewId, "REJECTED", report);
            throw new DayImportRejectedException(e instanceof DayImportRejectedException ? e.getMessage() : "Import rolled back; validate again", report);
        }
    }

    public DayImportReportDto report(UUID id) {
        DayImportAccess.requireImport();
        return ledger.run(id).report();
    }
}
