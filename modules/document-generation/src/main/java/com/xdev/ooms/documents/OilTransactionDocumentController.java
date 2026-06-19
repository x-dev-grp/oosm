package com.xdev.ooms.documents;

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
@RequestMapping("/api/documents/oil-transactions")
public class OilTransactionDocumentController {

    private final OilTransactionDocumentService oilTransactionDocumentService;

    public OilTransactionDocumentController(OilTransactionDocumentService oilTransactionDocumentService) {
        this.oilTransactionDocumentService = oilTransactionDocumentService;
    }

    @GetMapping(value = "/{id}/{type}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateOilTransactionDocument(
            @PathVariable UUID id,
            @PathVariable String type) {
        OilTransactionDocumentType documentType = OilTransactionDocumentType.fromPath(type);
        GeneratedDocument document = oilTransactionDocumentService.generate(id, documentType);
        return DocumentController.pdfResponse(document, ContentDisposition.attachment());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }
}
