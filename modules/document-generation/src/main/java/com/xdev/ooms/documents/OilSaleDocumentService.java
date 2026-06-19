package com.xdev.ooms.documents;

import com.xdev.ooms.documents.commercial.BillDocument;
import com.xdev.ooms.documents.commercial.OilSaleInvoicePdfService;
import com.xdev.ooms.documents.form.FormPdfGeneratorService;
import com.xdev.ooms.documents.form.dto.FormPdfDocument;
import com.xdev.ooms.documents.form.mapper.OilBonCommandePdfConfigMapper;
import com.xdev.ooms.documents.form.mapper.OilBonLivraisonPdfConfigMapper;
import com.xdev.ooms.production.oilsale.entity.OilSale;
import com.xdev.ooms.production.oilsale.repository.OilSaleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OilSaleDocumentService {

    private final OilSaleRepository oilSaleRepository;
    private final FormPdfGeneratorService formPdfGeneratorService;
    private final OilBonCommandePdfConfigMapper bonCommandePdfConfigMapper;
    private final OilBonLivraisonPdfConfigMapper bonLivraisonPdfConfigMapper;
    private final OilSaleInvoicePdfService oilSaleInvoicePdfService;

    public OilSaleDocumentService(
            OilSaleRepository oilSaleRepository,
            FormPdfGeneratorService formPdfGeneratorService,
            OilBonCommandePdfConfigMapper bonCommandePdfConfigMapper,
            OilBonLivraisonPdfConfigMapper bonLivraisonPdfConfigMapper,
            OilSaleInvoicePdfService oilSaleInvoicePdfService) {
        this.oilSaleRepository = oilSaleRepository;
        this.formPdfGeneratorService = formPdfGeneratorService;
        this.bonCommandePdfConfigMapper = bonCommandePdfConfigMapper;
        this.bonLivraisonPdfConfigMapper = bonLivraisonPdfConfigMapper;
        this.oilSaleInvoicePdfService = oilSaleInvoicePdfService;
    }

    @Transactional(readOnly = true)
    public GeneratedDocument generateBonCommande(UUID oilSaleId) {
        OilSale sale = oilSaleRepository.findByIdForPdf(oilSaleId)
                .orElseThrow(() -> new EntityNotFoundException("Oil sale not found: " + oilSaleId));

        FormPdfDocument document = formPdfGeneratorService.generate(bonCommandePdfConfigMapper.map(sale));
        return GeneratedDocument.fromForm(document.fileName(), document.content());
    }

    @Transactional
    public GeneratedDocument generateInvoice(UUID oilSaleId) {
        BillDocument document = oilSaleInvoicePdfService.generateInvoice(oilSaleId);
        return GeneratedDocument.fromBill(document);
    }

    @Transactional(readOnly = true)
    public GeneratedDocument generateBonLivraison(UUID oilSaleId) {
        OilSale sale = oilSaleRepository.findByIdForPdf(oilSaleId)
                .orElseThrow(() -> new EntityNotFoundException("Oil sale not found: " + oilSaleId));

        FormPdfDocument document = formPdfGeneratorService.generate(bonLivraisonPdfConfigMapper.map(sale));
        return GeneratedDocument.fromForm(document.fileName(), document.content());
    }
}
