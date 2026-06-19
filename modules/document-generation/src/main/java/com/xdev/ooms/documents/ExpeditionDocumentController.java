package com.xdev.ooms.documents;

import com.xdev.ooms.documents.expedition.ExpeditionDocumentService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
public class ExpeditionDocumentController {

    private final ExpeditionDocumentService expeditionDocumentService;

    public ExpeditionDocumentController(ExpeditionDocumentService expeditionDocumentService) {
        this.expeditionDocumentService = expeditionDocumentService;
    }

    @GetMapping(value = "/expeditions/{id}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateExpeditionPdf(@PathVariable UUID id) {
        GeneratedDocument document = expeditionDocumentService.generateExpeditionPdf(id);
        return DocumentController.pdfResponse(document, ContentDisposition.attachment());
    }

    @GetMapping(value = "/projects/{id}/traceability", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateProjectTraceabilityPdf(@PathVariable UUID id) {
        GeneratedDocument document = expeditionDocumentService.generateProjectTraceabilityPdf(id);
        return DocumentController.pdfResponse(document, ContentDisposition.attachment());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of("message", ex.getMessage()));
    }
}
