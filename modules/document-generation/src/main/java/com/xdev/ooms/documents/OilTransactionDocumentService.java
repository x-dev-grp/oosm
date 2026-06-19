package com.xdev.ooms.documents;

import com.xdev.ooms.documents.form.FormPdfGeneratorService;
import com.xdev.ooms.documents.form.dto.FormPdfConfigDto;
import com.xdev.ooms.documents.form.dto.FormPdfDocument;
import com.xdev.ooms.documents.form.mapper.OilSortiePdfConfigMapper;
import com.xdev.ooms.documents.form.mapper.OilTransactionReceiptPdfConfigMapper;
import com.xdev.ooms.production.oiltransaction.entity.OilTransaction;
import com.xdev.ooms.production.oiltransaction.repository.OilTransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OilTransactionDocumentService {

    private final OilTransactionRepository oilTransactionRepository;
    private final FormPdfGeneratorService formPdfGeneratorService;
    private final OilTransactionReceiptPdfConfigMapper receiptPdfConfigMapper;
    private final OilSortiePdfConfigMapper sortiePdfConfigMapper;

    public OilTransactionDocumentService(
            OilTransactionRepository oilTransactionRepository,
            FormPdfGeneratorService formPdfGeneratorService,
            OilTransactionReceiptPdfConfigMapper receiptPdfConfigMapper,
            OilSortiePdfConfigMapper sortiePdfConfigMapper) {
        this.oilTransactionRepository = oilTransactionRepository;
        this.formPdfGeneratorService = formPdfGeneratorService;
        this.receiptPdfConfigMapper = receiptPdfConfigMapper;
        this.sortiePdfConfigMapper = sortiePdfConfigMapper;
    }

    @Transactional(readOnly = true)
    public GeneratedDocument generate(UUID transactionId, OilTransactionDocumentType type) {
        OilTransaction transaction = oilTransactionRepository.findByIdForPdf(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Oil transaction not found: " + transactionId));

        FormPdfConfigDto config = switch (type) {
            case TRANSACTION -> receiptPdfConfigMapper.map(transaction);
            case SORTIE -> sortiePdfConfigMapper.map(transaction);
        };

        FormPdfDocument document = formPdfGeneratorService.generate(config);
        return GeneratedDocument.fromForm(document.fileName(), document.content());
    }
}
