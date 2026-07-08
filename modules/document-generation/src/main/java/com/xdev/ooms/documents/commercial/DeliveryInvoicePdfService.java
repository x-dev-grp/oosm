package com.xdev.ooms.documents.commercial;

import com.xdev.ooms.documents.commercial.dto.BillGenerationRequest;
import com.xdev.ooms.documents.commercial.dto.BillLineDto;
import com.xdev.ooms.documents.commercial.dto.BillPartyDto;
import com.xdev.ooms.production.supplier.entity.Supplier;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;
import com.xdev.ooms.production.unifieddelivery.repository.DeliveryRepository;
import com.xdev.ooms.sharedkernel.Enum.DeliveryType;
import com.xdev.ooms.sharedkernel.ports.CompanyProfileReadPort;
import com.xdev.ooms.sharedkernel.ports.CompanyProfileSnapshot;
import com.xdev.ooms.sharedkernel.ports.InvoiceNumberPort;
import com.xdev.ooms.sharedkernel.ports.UnifiedDeliveryBillLineDto;
import com.xdev.ooms.sharedkernel.ports.UnifiedDeliveryBillLinePort;
import com.xdev.ooms.sharedkernel.ports.UnifiedDeliveryBillLineQuery;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DeliveryInvoicePdfService {

    private final DeliveryRepository deliveryRepository;
    private final BillPdfGeneratorService billPdfGeneratorService;
    private final UnifiedDeliveryBillLinePort unifiedDeliveryBillLinePort;
    private final BillLabelResolver billLabelResolver;
    private final CompanyProfileReadPort companyProfileReadPort;
    private final InvoiceNumberPort invoiceNumberPort;
    private final BillVatPolicyResolver billVatPolicyResolver;

    public DeliveryInvoicePdfService(
            DeliveryRepository deliveryRepository,
            BillPdfGeneratorService billPdfGeneratorService,
            UnifiedDeliveryBillLinePort unifiedDeliveryBillLinePort,
            BillLabelResolver billLabelResolver,
            CompanyProfileReadPort companyProfileReadPort,
            InvoiceNumberPort invoiceNumberPort,
            BillVatPolicyResolver billVatPolicyResolver) {
        this.deliveryRepository = deliveryRepository;
        this.billPdfGeneratorService = billPdfGeneratorService;
        this.unifiedDeliveryBillLinePort = unifiedDeliveryBillLinePort;
        this.billLabelResolver = billLabelResolver;
        this.companyProfileReadPort = companyProfileReadPort;
        this.invoiceNumberPort = invoiceNumberPort;
        this.billVatPolicyResolver = billVatPolicyResolver;
    }

    @Transactional
    public BillDocument generateInvoice(UUID deliveryId) {
        UnifiedDelivery delivery = loadDelivery(deliveryId);
        ensureInvoiceNumber(delivery);
        CompanyProfileSnapshot profile = companyProfileReadPort.findCurrentTenantProfile()
                .orElseThrow(() -> new IllegalStateException("Company profile is required to generate invoice PDF"));

        BillGenerationRequest request = buildRequest(delivery, profile);
        BillDocument generated = billPdfGeneratorService.generate(request);

        String deliveryNumber = safe(delivery.getDeliveryNumber());
        String fileName = "Facture_" + (deliveryNumber.isBlank() ? "inconnu" : deliveryNumber) + ".pdf";
        return new BillDocument(generated.invoiceNumber(), fileName, generated.mediaType(), generated.content());
    }

    private BillGenerationRequest buildRequest(UnifiedDelivery delivery, CompanyProfileSnapshot profile) {
        BillGenerationRequest request = new BillGenerationRequest();
        request.setTitle("Facture commerciale");
        request.setInvoiceNumber(resolveInvoiceNumber(delivery));
        request.setOperationDate(delivery.getDeliveryDate() == null ? LocalDateTime.now() : delivery.getDeliveryDate());
        request.setCurrency("TND");
        request.setConditions(billLabelResolver.operationTypeLabel(delivery.getOperationType()));
        request.setLogoBase64(profile.logoBase64());
        request.setLogoContentType(profile.logoContentType());
        request.setIssuer(toIssuer(profile));
        request.setClient(toClient(delivery.getSupplier()));
        BillVatMode vatMode = billVatPolicyResolver.resolve(delivery.getOperationType(), null);
        request.setVatMode(vatMode);
        request.setLines(List.of(buildLine(delivery, vatMode)));
        request.setFooterContact(buildFooter(profile));
        request.setSourceType("UnifiedDelivery");
        request.setSourceId(delivery.getId());
        return request;
    }

    private BillLineDto buildLine(UnifiedDelivery delivery, BillVatMode vatMode) {
        BillLineDto line = new BillLineDto();
        String description = billLabelResolver.operationTypeLabel(delivery.getOperationType());
        if (description == null || description.isBlank()) {
            description = delivery.getDeliveryType() == DeliveryType.OIL ? "Huile d'olive" : "Olives";
        }
        line.setDesignation(description);
        line.setVatRatePercent(vatMode == BillVatMode.NONE
                ? BigDecimal.ZERO
                : TunisiaVatDefaults.resolvePurchaseRate(null));

        UnifiedDeliveryBillLineQuery query = new UnifiedDeliveryBillLineQuery(
                delivery.getId().toString(),
                delivery.getLotNumber(),
                delivery.getOperationType(),
                money(delivery.getPrice()));

        return unifiedDeliveryBillLinePort.resolve(query)
                .map(resolved -> applyResolvedLine(line, resolved))
                .orElseGet(() -> fallbackLine(line, delivery));
    }

    private BillLineDto applyResolvedLine(BillLineDto line, UnifiedDeliveryBillLineDto resolved) {
        line.setQuantity(resolved.quantity());
        line.setUnit(resolved.unit());
        line.setUnitPriceExcludingVat(resolved.unitPriceExcludingVat());
        return line;
    }

    private BillLineDto fallbackLine(BillLineDto line, UnifiedDelivery delivery) {
        boolean isOil = delivery.getDeliveryType() == DeliveryType.OIL;
        BigDecimal quantity = isOil ? money(delivery.getOilQuantity()) : money(delivery.getPoidsNet());
        line.setQuantity(quantity.signum() > 0 ? quantity : BigDecimal.ONE);
        line.setUnit(isOil ? "L" : "kg");
        line.setUnitPriceExcludingVat(money(delivery.getUnitPrice()));
        return line;
    }

    private BillPartyDto toIssuer(CompanyProfileSnapshot profile) {
        BillPartyDto issuer = new BillPartyDto();
        issuer.setDisplayName(profile.legalName());
        issuer.setAddress(profile.address());
        issuer.setTaxRegistrationNumber(profile.taxId());
        issuer.setPhone(profile.phone());
        issuer.setWebsite(profile.website());
        return issuer;
    }

    private BillPartyDto toClient(Supplier supplier) {
        BillPartyDto client = new BillPartyDto();
        if (supplier == null) {
            return client;
        }
        client.setDisplayName((safe(supplier.getName()) + " " + safe(supplier.getLastname())).trim());
        client.setTaxRegistrationNumber(supplier.getMatriculeFiscal());
        client.setAddress(supplier.getAddress());
        client.setPhone(supplier.getPhone());
        return client;
    }

    private com.xdev.ooms.documents.commercial.dto.BillFooterContactDto buildFooter(CompanyProfileSnapshot profile) {
        com.xdev.ooms.documents.commercial.dto.BillFooterContactDto footer =
                new com.xdev.ooms.documents.commercial.dto.BillFooterContactDto();
        footer.setCompanyName(profile.legalName());
        footer.setPhone(profile.phone());
        return footer;
    }

    private UnifiedDelivery loadDelivery(UUID deliveryId) {
        return deliveryRepository.findByIdForPdf(deliveryId)
                .orElseThrow(() -> new EntityNotFoundException("Delivery not found: " + deliveryId));
    }

    private BigDecimal money(Double value) {
        return value == null ? BigDecimal.ZERO : BigDecimal.valueOf(value);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String resolveInvoiceNumber(UnifiedDelivery delivery) {
        if (delivery.getInvoiceNumber() != null && !delivery.getInvoiceNumber().isBlank()) {
            return delivery.getInvoiceNumber().trim();
        }
        if (delivery.getDeliveryNumber() != null && !delivery.getDeliveryNumber().isBlank()) {
            return delivery.getDeliveryNumber().trim();
        }
        return delivery.getId() == null ? "inconnu" : delivery.getId().toString();
    }

    private void ensureInvoiceNumber(UnifiedDelivery delivery) {
        if (delivery.getInvoiceNumber() != null && !delivery.getInvoiceNumber().isBlank()) {
            return;
        }
        delivery.setInvoiceNumber(invoiceNumberPort.nextInvoiceNumber());
        deliveryRepository.save(delivery);
    }
}
