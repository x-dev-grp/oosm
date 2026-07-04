package com.xdev.ooms.documents;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents/deliveries")
public class DocumentController {

    private final DocumentGenerationService documentGenerationService;

    public DocumentController(DocumentGenerationService documentGenerationService) {
        this.documentGenerationService = documentGenerationService;
    }

    @GetMapping(value = "/{id}/{type}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateDeliveryDocument(@PathVariable UUID id, @PathVariable String type) {
        DocumentType documentType = DocumentType.fromPath(type);
        GeneratedDocument document = documentGenerationService.generateDeliveryDocument(id, documentType);
        return pdfResponse(document, ContentDisposition.attachment());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    public static ResponseEntity<byte[]> pdfResponse(GeneratedDocument document, ContentDisposition.Builder disposition) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(document.mediaType()));
        headers.setContentDisposition(disposition.filename(document.fileName()).build());
        headers.setContentLength(document.content().length);
        return ResponseEntity.ok().headers(headers).body(document.content());
    }
}
