package com.xdev.ooms.production.dayimport.controller;

import com.xdev.ooms.production.dayimport.dto.*;
import com.xdev.ooms.production.dayimport.service.*;
import com.xdev.ooms.sharedkernel.communicator.models.shared.ApiResponse;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@RestController
@RequestMapping("/api/production/import/day")
public class DayImportController {
    private final DayImportService engine;
    private final DayImportWorkflow workflow;
    private final DayImportReportExporter exporter;
    private final DayImportDriveService drive;
    public DayImportController(DayImportService engine, DayImportWorkflow workflow, DayImportReportExporter exporter, DayImportDriveService drive) {
        this.engine = engine; this.workflow = workflow; this.exporter = exporter; this.drive = drive;
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> template(@RequestParam(required=false) String lang) throws Exception { DayImportAccess.requireImport(); return download(engine.blankTemplate(lang), "oosm-day-import-template.xlsx", false); }
    @GetMapping("/sample")
    public ResponseEntity<byte[]> sample(@RequestParam(required=false) String lang) throws Exception { DayImportAccess.requireImport(); return download(engine.sampleTemplate(lang), "oosm-day-import-sample.xlsx", false); }

    @PostMapping(value="/dry-run", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DayImportReportDto>> dryRun(@RequestPart("file") MultipartFile file) throws Exception {
        DayImportAccess.requireImport();
        return ResponseEntity.ok(new ApiResponse<>(true, "Validation complete", workflow.preview(bytes(file), "MANUAL")));
    }

    @PostMapping(value="/commit", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DayImportReportDto>> commit(@RequestPart("file") MultipartFile file, @RequestParam UUID previewId) throws Exception {
        DayImportAccess.requireImport();
        return ResponseEntity.ok(new ApiResponse<>(true, "Import complete", workflow.commit(bytes(file), previewId)));
    }

    @GetMapping("/runs/{id}")
    public ResponseEntity<ApiResponse<DayImportReportDto>> run(@PathVariable UUID id) {
        DayImportAccess.requireImport();
        return ResponseEntity.ok(new ApiResponse<>(true, "Import result", workflow.report(id)));
    }

    @GetMapping("/runs/{id}/report")
    public ResponseEntity<byte[]> savedReport(@PathVariable UUID id, @RequestParam(defaultValue="xlsx") String format) throws Exception {
        DayImportAccess.requireImport();
        return export(workflow.report(id), format);
    }

    @PostMapping(value="/dry-run/report", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> dryRunReport(@RequestPart("file") MultipartFile file, @RequestParam(defaultValue="xlsx") String format) throws Exception {
        DayImportAccess.requireImport();
        return export(engine.dryRun(bytes(file)), format);
    }

    @GetMapping("/drive/status")
    public ResponseEntity<ApiResponse<DayImportDriveStatusDto>> driveStatus() {
        DayImportAccess.requireImport();
        return ResponseEntity.ok(new ApiResponse<>(true, "Drive status", drive.status()));
    }
    @PostMapping("/drive/sync")
    public ResponseEntity<ApiResponse<DayImportDriveStatusDto>> driveSync() {
        DayImportAccess.requireAdmin();
        return ResponseEntity.ok(new ApiResponse<>(true, "Drive sync", drive.syncNow()));
    }
    @GetMapping("/drive/oauth/authorize")
    public ResponseEntity<ApiResponse<Map<String,String>>> driveAuthorize() {
        DayImportAccess.requireAdmin();
        return ResponseEntity.ok(new ApiResponse<>(true, "Authorize URL", drive.beginAuthorize()));
    }
    @PostMapping("/drive/oauth/disconnect")
    public ResponseEntity<ApiResponse<DayImportDriveStatusDto>> driveDisconnect() {
        DayImportAccess.requireAdmin();
        return ResponseEntity.ok(new ApiResponse<>(true, "Disconnected", drive.disconnect()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> denied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, e.getMessage(), null));
    }
    @ExceptionHandler(DayImportRejectedException.class)
    public ResponseEntity<ApiResponse<DayImportReportDto>> rejected(DayImportRejectedException e) {
        return ResponseEntity.unprocessableEntity().body(new ApiResponse<>(false, e.getMessage(), e.getReport()));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> invalid(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
    }

    private byte[] bytes(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty() || file.getOriginalFilename() == null || !file.getOriginalFilename().toLowerCase(Locale.ROOT).endsWith(".xlsx"))
            throw new IllegalArgumentException("Select a nonempty XLSX workbook");
        if (file.getSize() > 20 * 1024 * 1024) throw new IllegalArgumentException("Workbook exceeds 20 MB");
        return file.getBytes();
    }
    private ResponseEntity<byte[]> export(DayImportReportDto report, String format) throws Exception {
        if (!Set.of("csv", "xlsx").contains(format)) throw new IllegalArgumentException("Report format must be csv or xlsx");
        return download("csv".equals(format) ? exporter.toCsv(report) : exporter.toXlsx(report), "day-import-report." + format, "csv".equals(format));
    }
    private ResponseEntity<byte[]> download(byte[] bytes, String filename, boolean csv) {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(csv ? "text/csv" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).body(bytes);
    }
}
