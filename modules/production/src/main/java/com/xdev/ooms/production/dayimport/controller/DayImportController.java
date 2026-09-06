package com.xdev.ooms.production.dayimport.controller;

import com.xdev.ooms.production.dayimport.dto.DayImportDriveStatusDto;
import com.xdev.ooms.production.dayimport.dto.DayImportReportDto;
import com.xdev.ooms.production.dayimport.service.DayImportDriveService;
import com.xdev.ooms.production.dayimport.service.DayImportReportExporter;
import com.xdev.ooms.production.dayimport.service.DayImportService;
import com.xdev.ooms.sharedkernel.communicator.models.shared.ApiResponse;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.ports.SupportTicketCreateCommand;
import com.xdev.ooms.sharedkernel.ports.SupportTicketPort;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/production/import/day")
public class DayImportController {

    private final DayImportService dayImportService;
    private final DayImportReportExporter reportExporter;
    private final DayImportDriveService driveService;
    private final ObjectProvider<SupportTicketPort> supportTicketPort;

    public DayImportController(
            DayImportService dayImportService,
            DayImportReportExporter reportExporter,
            DayImportDriveService driveService,
            ObjectProvider<SupportTicketPort> supportTicketPort) {
        this.dayImportService = dayImportService;
        this.reportExporter = reportExporter;
        this.driveService = driveService;
        this.supportTicketPort = supportTicketPort;
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> template() throws Exception {
        OOSMLogger.logMethodEntry(getClass(), "template");
        byte[] bytes = dayImportService.blankTemplate();
        OOSMLogger.logMethodExit(getClass(), "template", bytes != null ? bytes.length : 0);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"oosm-day-import-template.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/sample")
    public ResponseEntity<byte[]> sample() throws Exception {
        OOSMLogger.logMethodEntry(getClass(), "sample");
        byte[] bytes = dayImportService.sampleTemplate();
        OOSMLogger.logMethodExit(getClass(), "sample", bytes != null ? bytes.length : 0);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"oosm-day-import-sample.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @PostMapping(value = "/dry-run", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DayImportReportDto>> dryRun(@RequestPart("file") MultipartFile file) throws Exception {
        long start = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(getClass(), "dryRun",
                file != null ? file.getOriginalFilename() : null,
                file != null ? file.getSize() : -1);
        try {
            DayImportReportDto report = dayImportService.dryRun(file);
            OOSMLogger.logPerformance(getClass(), "dryRun", start, System.currentTimeMillis());
            return ResponseEntity.ok(new ApiResponse<>(true, "Dry-run complete", report));
        } catch (Exception e) {
            OOSMLogger.logException(getClass(), "dryRun endpoint failed", e);
            return ResponseEntity.ok(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping(value = "/dry-run/report", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> dryRunReport(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "xlsx") String format) throws Exception {
        OOSMLogger.logMethodEntry(getClass(), "dryRunReport",
                file != null ? file.getOriginalFilename() : null, format);
        try {
            DayImportReportDto report = dayImportService.dryRun(file);
            if ("csv".equalsIgnoreCase(format)) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"day-import-report.csv\"")
                        .contentType(MediaType.parseMediaType("text/csv"))
                        .body(reportExporter.toCsv(report));
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"day-import-report.xlsx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(reportExporter.toXlsx(report));
        } catch (Exception e) {
            OOSMLogger.logException(getClass(), "dryRunReport endpoint failed", e);
            throw e;
        }
    }

    @PostMapping(value = "/commit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DayImportReportDto>> commit(@RequestPart("file") MultipartFile file) throws Exception {
        long start = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(getClass(), "commit",
                file != null ? file.getOriginalFilename() : null,
                file != null ? file.getSize() : -1);
        try {
            DayImportReportDto report = dayImportService.commit(file);
            OOSMLogger.logPerformance(getClass(), "commit", start, System.currentTimeMillis());
            return ResponseEntity.ok(new ApiResponse<>(true, "Commit complete", report));
        } catch (Exception e) {
            OOSMLogger.logException(getClass(), "commit endpoint failed", e);
            String ticketId = openCommitFailureTicket(file, e);
            String message = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            if (ticketId != null) {
                message = message + " | Support ticket created: " + ticketId;
            }
            return ResponseEntity.ok(new ApiResponse<>(false, message, null));
        }
    }

    private String openCommitFailureTicket(MultipartFile file, Exception e) {
        SupportTicketPort port = supportTicketPort.getIfAvailable();
        if (port == null) {
            return null;
        }
        String fileName = file != null ? file.getOriginalFilename() : "(unknown)";
        String subject = "Day Excel import commit failed";
        if (subject.length() > 160) {
            subject = subject.substring(0, 160);
        }
        String description = """
                Automatic ticket: day Excel import commit failed.

                Tenant: %s
                File: %s
                Error: %s

                Page: /reception/import
                """.formatted(
                TenantContext.getCurrentTenant(),
                fileName,
                e.getMessage() != null ? e.getMessage() : e.toString()
        );
        try {
            return port.create(new SupportTicketCreateCommand(
                    subject, description, "HIGH", "/reception/import"));
        } catch (Exception ticketEx) {
            OOSMLogger.logException(getClass(), "Unable to open support ticket after commit failure", ticketEx);
            return null;
        }
    }

    @PostMapping("/drive/sync")
    public ResponseEntity<ApiResponse<DayImportDriveStatusDto>> driveSync() {
        OOSMLogger.logMethodEntry(getClass(), "driveSync");
        DayImportDriveStatusDto status = driveService.syncNow();
        OOSMLogger.logBusinessEvent(getClass(), "DAY_IMPORT_DRIVE_SYNC",
                "result=" + status.getLastResult() + " error=" + status.getLastError());
        return ResponseEntity.ok(new ApiResponse<>(true, "Drive sync", status));
    }

    @GetMapping("/drive/status")
    public ResponseEntity<ApiResponse<DayImportDriveStatusDto>> driveStatus() {
        OOSMLogger.logMethodEntry(getClass(), "driveStatus");
        return ResponseEntity.ok(new ApiResponse<>(true, "Drive status", driveService.status()));
    }

    @GetMapping("/drive/oauth/authorize")
    public ResponseEntity<ApiResponse<java.util.Map<String, String>>> driveAuthorize() {
        OOSMLogger.logMethodEntry(getClass(), "driveAuthorize");
        try {
            return ResponseEntity.ok(new ApiResponse<>(true, "Authorize URL", driveService.beginAuthorize()));
        } catch (Exception e) {
            OOSMLogger.logException(getClass(), "driveAuthorize failed", e);
            return ResponseEntity.ok(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping("/drive/oauth/disconnect")
    public ResponseEntity<ApiResponse<DayImportDriveStatusDto>> driveDisconnect() {
        OOSMLogger.logMethodEntry(getClass(), "driveDisconnect");
        OOSMLogger.logBusinessEvent(getClass(), "DAY_IMPORT_DRIVE_DISCONNECT", "tenant disconnect requested");
        return ResponseEntity.ok(new ApiResponse<>(true, "Disconnected", driveService.disconnect()));
    }
}
