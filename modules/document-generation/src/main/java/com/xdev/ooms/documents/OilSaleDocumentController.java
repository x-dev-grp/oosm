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
@RequestMapping("/api/documents/oil-sales")
public class OilSaleDocumentController {

    private final OilSaleDocumentService oilSaleDocumentService;

    public OilSaleDocumentController(OilSaleDocumentService oilSaleDocumentService) {
        this.oilSaleDocumentService = oilSaleDocumentService;
    }

    @GetMapping(value = "/{id}/bon-commande", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateBonCommande(@PathVariable UUID id) {
        GeneratedDocument document = oilSaleDocumentService.generateBonCommande(id);
        return DocumentController.pdfResponse(document, ContentDisposition.attachment());
    }

    @GetMapping(value = "/{id}/invoice", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateInvoice(@PathVariable UUID id) {
        GeneratedDocument document = oilSaleDocumentService.generateInvoice(id);
        return DocumentController.pdfResponse(document, ContentDisposition.attachment());
    }

    @GetMapping(value = "/{id}/bon-livraison", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateBonLivraison(@PathVariable UUID id) {
        GeneratedDocument document = oilSaleDocumentService.generateBonLivraison(id);
        return DocumentController.pdfResponse(document, ContentDisposition.attachment());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }
}
