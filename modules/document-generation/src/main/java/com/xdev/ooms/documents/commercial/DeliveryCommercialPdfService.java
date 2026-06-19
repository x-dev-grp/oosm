package com.xdev.ooms.documents.commercial;

import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;
import com.xdev.ooms.production.unifieddelivery.repository.DeliveryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class DeliveryCommercialPdfService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryInvoicePdfService deliveryInvoicePdfService;
    private final PaymentNotePdfGeneratorService paymentNotePdfGeneratorService;

    public DeliveryCommercialPdfService(
            DeliveryRepository deliveryRepository,
            DeliveryInvoicePdfService deliveryInvoicePdfService,
            PaymentNotePdfGeneratorService paymentNotePdfGeneratorService) {
        this.deliveryRepository = deliveryRepository;
        this.deliveryInvoicePdfService = deliveryInvoicePdfService;
        this.paymentNotePdfGeneratorService = paymentNotePdfGeneratorService;
    }

    @Transactional(readOnly = true)
    public BillDocument generateCommercialPdf(UUID deliveryId) {
        UnifiedDelivery delivery = deliveryRepository.findByIdForPdf(deliveryId)
                .orElseThrow(() -> new EntityNotFoundException("Delivery not found: " + deliveryId));
        if (resolveUnpaid(delivery).signum() > 0) {
            return paymentNotePdfGeneratorService.generatePaymentNote(deliveryId);
        }
        return deliveryInvoicePdfService.generateInvoice(deliveryId);
    }

    private BigDecimal resolveUnpaid(UnifiedDelivery delivery) {
        if (delivery.getUnpaidAmount() != null && delivery.getUnpaidAmount() > 0) {
            return BigDecimal.valueOf(delivery.getUnpaidAmount());
        }
        double total = delivery.getPrice() == null ? 0d : delivery.getPrice();
        double paid = delivery.getPaidAmount() == null ? 0d : delivery.getPaidAmount();
        double rest = total - paid;
        return rest > 0 ? BigDecimal.valueOf(rest) : BigDecimal.ZERO;
    }
}
