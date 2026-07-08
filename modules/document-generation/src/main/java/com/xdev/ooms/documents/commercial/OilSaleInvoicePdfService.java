package com.xdev.ooms.documents.commercial;

import com.xdev.ooms.documents.commercial.dto.BillFooterContactDto;
import com.xdev.ooms.documents.commercial.dto.BillGenerationRequest;
import com.xdev.ooms.documents.commercial.dto.BillPartyDto;
import com.xdev.ooms.production.oilsale.entity.OilSale;
import com.xdev.ooms.production.oilsale.repository.OilSaleRepository;
import com.xdev.ooms.production.supplier.entity.Supplier;
import com.xdev.ooms.sharedkernel.Enum.Currency;
import com.xdev.ooms.sharedkernel.ports.CompanyProfileReadPort;
import com.xdev.ooms.sharedkernel.ports.CompanyProfileSnapshot;
import com.xdev.ooms.sharedkernel.ports.InvoiceNumberPort;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OilSaleInvoicePdfService {

    private final OilSaleRepository oilSaleRepository;
    private final OilSaleBillLineBuilder oilSaleBillLineBuilder;
    private final BillPdfGeneratorService billPdfGeneratorService;
    private final CompanyProfileReadPort companyProfileReadPort;
    private final InvoiceNumberPort invoiceNumberPort;

    public OilSaleInvoicePdfService(
            OilSaleRepository oilSaleRepository,
            OilSaleBillLineBuilder oilSaleBillLineBuilder,
            BillPdfGeneratorService billPdfGeneratorService,
            CompanyProfileReadPort companyProfileReadPort,
            InvoiceNumberPort invoiceNumberPort) {
        this.oilSaleRepository = oilSaleRepository;
        this.oilSaleBillLineBuilder = oilSaleBillLineBuilder;
        this.billPdfGeneratorService = billPdfGeneratorService;
        this.companyProfileReadPort = companyProfileReadPort;
        this.invoiceNumberPort = invoiceNumberPort;
    }

    @Transactional
    public BillDocument generateInvoice(UUID oilSaleId) {
        OilSale sale = oilSaleRepository.findByIdForPdf(oilSaleId)
                .orElseThrow(() -> new EntityNotFoundException("Oil sale not found: " + oilSaleId));
        ensureInvoiceNumber(sale);
        CompanyProfileSnapshot profile = companyProfileReadPort.findCurrentTenantProfile()
                .orElseThrow(() -> new IllegalStateException("Company profile is required to generate invoice PDF"));

        BillGenerationRequest request = buildRequest(sale, profile);
        BillDocument generated = billPdfGeneratorService.generate(request);

        String invoiceRef = resolveInvoiceNumber(sale);
        String fileName = "Facture_" + invoiceRef + ".pdf";
        return new BillDocument(generated.invoiceNumber(), fileName, generated.mediaType(), generated.content());
    }

    private BillGenerationRequest buildRequest(OilSale sale, CompanyProfileSnapshot profile) {
        BillGenerationRequest request = new BillGenerationRequest();
        request.setTitle("Facture commerciale");
        request.setInvoiceNumber(resolveInvoiceNumber(sale));
        request.setOperationDate(sale.getSaleDate() != null ? sale.getSaleDate() : LocalDateTime.now());
        request.setCurrency(sale.getCurrency() != null ? sale.getCurrency().name() : Currency.TND.name());
        request.setConditions(oilSaleBillLineBuilder.containerSummary(sale.getId()).isBlank()
                ? "Vente huile"
                : "Vente huile et conteneurs");
        request.setPaymentMethod(sale.getPaymentMethod() != null ? sale.getPaymentMethod().name() : null);
        request.setLogoBase64(profile.logoBase64());
        request.setLogoContentType(profile.logoContentType());
        request.setIssuer(toIssuer(profile));
        request.setClient(toClient(sale));
        request.setLines(oilSaleBillLineBuilder.buildLines(sale));
        request.setFooterContact(buildFooter(profile));
        request.setSourceType("OilSale");
        request.setSourceId(sale.getId());
        request.setVatMode(BillVatMode.NONE);
        if (sale.getDescription() != null && !sale.getDescription().isBlank()) {
            request.setNotes(sale.getDescription().trim());
        }
        return request;
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

    private BillPartyDto toClient(OilSale sale) {
        BillPartyDto client = new BillPartyDto();
        if (sale == null) {
            return client;
        }
        Supplier supplier = sale.getSupplier();
        if (supplier != null) {
            client.setDisplayName((safe(supplier.getName()) + " " + safe(supplier.getLastname())).trim());
            client.setTaxRegistrationNumber(supplier.getMatriculeFiscal());
            client.setPhone(supplier.getPhone());
        }
        client.setAddress(resolveClientAddress(sale, supplier));
        return client;
    }

    private String resolveClientAddress(OilSale sale, Supplier supplier) {
        if (sale.getDeliveryAddress() != null && !sale.getDeliveryAddress().isBlank()) {
            return sale.getDeliveryAddress().trim();
        }
        if (supplier != null && supplier.getAddress() != null && !supplier.getAddress().isBlank()) {
            return supplier.getAddress().trim();
        }
        return null;
    }

    private BillFooterContactDto buildFooter(CompanyProfileSnapshot profile) {
        BillFooterContactDto footer =
                new BillFooterContactDto();
        footer.setCompanyName(profile.legalName());
        footer.setPhone(profile.phone());
        return footer;
    }

    private String resolveInvoiceNumber(OilSale sale) {
        if (sale.getInvoiceNumber() != null && !sale.getInvoiceNumber().isBlank()) {
            return sale.getInvoiceNumber().trim();
        }
        return sale.getId() == null ? "inconnu" : sale.getId().toString();
    }

    private void ensureInvoiceNumber(OilSale sale) {
        if (sale.getInvoiceNumber() != null && !sale.getInvoiceNumber().isBlank()) {
            return;
        }
        sale.setInvoiceNumber(invoiceNumberPort.nextInvoiceNumber());
        oilSaleRepository.save(sale);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
