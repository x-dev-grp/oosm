package com.xdev.ooms.finance.billing.controller;

import com.xdev.ooms.documents.commercial.BillDocument;
import com.xdev.ooms.documents.commercial.BillPdfGeneratorService;
import com.xdev.ooms.documents.commercial.dto.BillGenerationRequest;
import com.xdev.ooms.documents.commercial.dto.TransactionBillRequest;
import com.xdev.ooms.finance.billing.service.TransactionBillMapper;
import com.xdev.ooms.finance.financialtransaction.entity.FinancialTransaction;
import com.xdev.ooms.finance.financialtransaction.repository.FinancialTransactionRepository;
import com.xdev.ooms.finance.financialtransaction.service.FinancialTransactionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/finance/bills")
public class BillController {

    private final BillPdfGeneratorService billPdfGeneratorService;
    private final FinancialTransactionRepository financialTransactionRepository;
    private final FinancialTransactionService financialTransactionService;
    private final TransactionBillMapper transactionBillMapper;

    public BillController(
            BillPdfGeneratorService billPdfGeneratorService,
            FinancialTransactionRepository financialTransactionRepository,
            FinancialTransactionService financialTransactionService,
            TransactionBillMapper transactionBillMapper) {
        this.billPdfGeneratorService = billPdfGeneratorService;
        this.financialTransactionRepository = financialTransactionRepository;
        this.financialTransactionService = financialTransactionService;
        this.transactionBillMapper = transactionBillMapper;
    }

    @PostMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generatePdf(@RequestBody BillGenerationRequest request) {
        return pdfResponse(billPdfGeneratorService.generate(request));
    }

    @PostMapping(value = "/transactions/{transactionId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateTransactionPdf(
            @PathVariable UUID transactionId,
            @RequestBody(required = false) TransactionBillRequest request) {
        financialTransactionService.ensureInvoiceReference(transactionId);
        FinancialTransaction tx = financialTransactionRepository.findByIdAndIsDeletedFalse(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Financial transaction not found: " + transactionId));
        return pdfResponse(billPdfGeneratorService.generate(transactionBillMapper.fromTransaction(tx, request)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    private ResponseEntity<byte[]> pdfResponse(BillDocument document) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline().filename(document.fileName()).build());
        headers.setContentLength(document.content().length);
        return ResponseEntity.ok().headers(headers).body(document.content());
    }
}
