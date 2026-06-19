package com.xdev.ooms.documents;

import com.xdev.ooms.documents.commercial.DeliveryCommercialPdfService;
import com.xdev.ooms.documents.commercial.DeliveryInvoicePdfService;
import com.xdev.ooms.documents.commercial.PaymentNotePdfGeneratorService;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Single entry point for all UnifiedDelivery PDF generation.
 */
@Service
public class DocumentGenerationService {

    private final FormDeliveryDocumentService formDeliveryDocumentService;
    private final DeliveryInvoicePdfService deliveryInvoicePdfService;
    private final PaymentNotePdfGeneratorService paymentNotePdfGeneratorService;
    private final DeliveryCommercialPdfService deliveryCommercialPdfService;

    public DocumentGenerationService(
            FormDeliveryDocumentService formDeliveryDocumentService,
            DeliveryInvoicePdfService deliveryInvoicePdfService,
            PaymentNotePdfGeneratorService paymentNotePdfGeneratorService,
            DeliveryCommercialPdfService deliveryCommercialPdfService) {
        this.formDeliveryDocumentService = formDeliveryDocumentService;
        this.deliveryInvoicePdfService = deliveryInvoicePdfService;
        this.paymentNotePdfGeneratorService = paymentNotePdfGeneratorService;
        this.deliveryCommercialPdfService = deliveryCommercialPdfService;
    }

    public GeneratedDocument generateDeliveryDocument(UUID deliveryId, DocumentType type) {
        return switch (type) {
            case RECEPTION, QUALITY_CONTROL, PRODUCTION -> formDeliveryDocumentService.generate(deliveryId, type);
            case COMMERCIAL_INVOICE -> GeneratedDocument.fromBill(deliveryInvoicePdfService.generateInvoice(deliveryId));
            case PAYMENT_NOTE -> GeneratedDocument.fromBill(paymentNotePdfGeneratorService.generatePaymentNote(deliveryId));
            case COMMERCIAL -> GeneratedDocument.fromBill(deliveryCommercialPdfService.generateCommercialPdf(deliveryId));
        };
    }
}
