package com.xdev.ooms.finance.billing.service;

import com.xdev.ooms.documents.commercial.BillLabelResolver;
import com.xdev.ooms.documents.commercial.BillVatMode;
import com.xdev.ooms.documents.commercial.BillVatPolicyResolver;
import com.xdev.ooms.documents.commercial.OilSaleBillLineBuilder;
import com.xdev.ooms.documents.commercial.TunisiaVatDefaults;
import com.xdev.ooms.documents.commercial.dto.BillBankInfoDto;
import com.xdev.ooms.documents.commercial.dto.BillFooterContactDto;
import com.xdev.ooms.documents.commercial.dto.BillGenerationRequest;
import com.xdev.ooms.documents.commercial.dto.BillLineDto;
import com.xdev.ooms.documents.commercial.dto.BillLogisticsDto;
import com.xdev.ooms.documents.commercial.dto.BillPartyDto;
import com.xdev.ooms.documents.commercial.dto.TransactionBillRequest;
import com.xdev.ooms.finance.financialtransaction.entity.FinancialTransaction;
import com.xdev.ooms.production.oilsale.entity.OilSale;
import com.xdev.ooms.production.oilsale.repository.OilSaleRepository;
import com.xdev.ooms.production.supplier.entity.Supplier;
import com.xdev.ooms.production.supplier.repository.SupplierRepository;
import com.xdev.ooms.sharedkernel.Enum.ResourceName;
import com.xdev.ooms.sharedkernel.ports.UnifiedDeliveryBillLinePort;
import com.xdev.ooms.sharedkernel.ports.UnifiedDeliveryBillLineQuery;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class TransactionBillMapper {

    private final SupplierRepository supplierRepository;
    private final EntityManagerFactory entityManagerFactory;
    private final BillLabelResolver billLabelResolver;
    private final UnifiedDeliveryBillLinePort unifiedDeliveryBillLinePort;
    private final OilSaleRepository oilSaleRepository;
    private final OilSaleBillLineBuilder oilSaleBillLineBuilder;
    private final BillVatPolicyResolver billVatPolicyResolver;

    public TransactionBillMapper(
            SupplierRepository supplierRepository,
            EntityManagerFactory entityManagerFactory,
            BillLabelResolver billLabelResolver,
            UnifiedDeliveryBillLinePort unifiedDeliveryBillLinePort,
            OilSaleRepository oilSaleRepository,
            OilSaleBillLineBuilder oilSaleBillLineBuilder,
            BillVatPolicyResolver billVatPolicyResolver) {
        this.supplierRepository = supplierRepository;
        this.entityManagerFactory = entityManagerFactory;
        this.billLabelResolver = billLabelResolver;
        this.unifiedDeliveryBillLinePort = unifiedDeliveryBillLinePort;
        this.oilSaleRepository = oilSaleRepository;
        this.oilSaleBillLineBuilder = oilSaleBillLineBuilder;
        this.billVatPolicyResolver = billVatPolicyResolver;
    }

    public BillGenerationRequest fromTransaction(FinancialTransaction tx, TransactionBillRequest options) {
        TransactionBillRequest safeOptions = options == null ? new TransactionBillRequest() : options;
        BillGenerationRequest request = new BillGenerationRequest();
        request.setInvoiceNumber(firstNonBlank(tx.getInvoiceReference(), tx.getReceiptReference(), fallbackInvoiceNumber(tx)));
        request.setOperationDate(tx.getTransactionDate() == null ? LocalDateTime.now() : tx.getTransactionDate());
        request.setCurrency(tx.getCurrency() == null ? "TND" : tx.getCurrency().name());
        request.setTitle(firstNonBlank(safeOptions.getTitle(), "Facture commerciale"));
        request.setConditions(firstNonBlank(safeOptions.getConditions(), defaultConditions(tx)));
        request.setLogoBase64(safeOptions.getLogoBase64());
        request.setLogoContentType(safeOptions.getLogoContentType());
        request.setIssuer(safeOptions.getIssuer());
        request.setClient(resolveClient(tx, safeOptions));
        request.setLogistics(safeOptions.getLogistics());
        request.setBankInfo(safeOptions.getBankInfo());
        request.setPaymentTerms(safeOptions.getPaymentTerms() == null ? new ArrayList<>() : new ArrayList<>(safeOptions.getPaymentTerms()));
        request.setFooterContact(resolveFooterContact(safeOptions));
        request.setSourceType("FinancialTransaction");
        request.setSourceId(tx.getId());
        request.setPaymentMethod(tx.getPaymentMethod() == null ? null : tx.getPaymentMethod().name());
        request.setElectronicInvoice(safeOptions.isElectronicInvoice());
        request.setTtnReference(safeOptions.getTtnReference());
        request.setIssuerElectronicSeal(safeOptions.getIssuerElectronicSeal());
        request.setNotes(safeOptions.getNotes());
        request.setVatMode(billVatPolicyResolver.resolve(tx.getOperationType(), tx.getTransactionType()));

        Optional<OilSale> linkedOilSale = resolveLinkedOilSale(tx);
        if (linkedOilSale.isPresent()) {
            OilSale sale = linkedOilSale.get();
            request.setLines(oilSaleBillLineBuilder.buildLines(sale));
            request.setVatMode(BillVatMode.NONE);
            request.setInvoiceNumber(firstNonBlank(sale.getInvoiceNumber(), request.getInvoiceNumber()));
            request.setSourceType("OilSale");
            request.setSourceId(sale.getId());
            if (oilSaleBillLineBuilder.containerSummary(sale.getId()).isBlank()) {
                request.setConditions(firstNonBlank(safeOptions.getConditions(), "Vente huile"));
            } else {
                request.setConditions(firstNonBlank(safeOptions.getConditions(), "Vente huile et conteneurs"));
            }
            return request;
        }

        BillLineDto line = buildLine(tx, safeOptions);
        request.setLines(List.of(line));
        return request;
    }

    private Optional<OilSale> resolveLinkedOilSale(FinancialTransaction tx) {
        if (tx.getResourceName() != ResourceName.OILSALE) {
            return Optional.empty();
        }
        String reference = tx.getExternalTransactionId();
        if (reference == null || reference.isBlank()) {
            return Optional.empty();
        }
        try {
            UUID saleId = UUID.fromString(reference.trim());
            return oilSaleRepository.findByIdForPdf(saleId)
                    .or(() -> oilSaleRepository.findByIdAndIsDeletedFalse(saleId));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private BillLineDto buildLine(FinancialTransaction tx, TransactionBillRequest safeOptions) {
        BillLineDto line = new BillLineDto();
        line.setDesignation(billLabelResolver.resolveDesignation(
                firstNonBlank(safeOptions.getDesignation(), tx.getDescription()),
                tx.getOperationType(),
                tx.getTransactionType()));
        line.setVatRatePercent(TunisiaVatDefaults.resolvePurchaseRate(safeOptions.getVatRatePercent()));

        UnifiedDeliveryBillLineQuery query = new UnifiedDeliveryBillLineQuery(
                tx.getExternalTransactionId(),
                tx.getLotNumber(),
                tx.getOperationType(),
                tx.getAmount());

        return unifiedDeliveryBillLinePort.resolve(query)
                .filter(resolved -> resolved.quantity().signum() > 0)
                .map(resolved -> {
                    line.setQuantity(resolved.quantity());
                    line.setUnit(resolved.unit());
                    line.setUnitPriceExcludingVat(resolved.unitPriceExcludingVat());
                    return line;
                })
                .orElseGet(() -> {
                    line.setQuantity(BigDecimal.ONE);
                    line.setUnit("SERVICE");
                    line.setUnitPriceExcludingVat(normalizedAmount(tx));
                    return line;
                });
    }

    private BillFooterContactDto resolveFooterContact(TransactionBillRequest options) {
        if (options.getFooterContact() != null) {
            return options.getFooterContact();
        }
        BillPartyDto issuer = options.getIssuer();
        if (issuer == null) {
            return null;
        }
        BillFooterContactDto footer = new BillFooterContactDto();
        footer.setCompanyName(issuer.getDisplayName());
        footer.setPhone(issuer.getPhone());
        return footer;
    }

    private String defaultConditions(FinancialTransaction tx) {
        return billLabelResolver.resolveConditions(tx.getOperationType(), tx.getTransactionType());
    }

    private BillPartyDto resolveClient(FinancialTransaction tx, TransactionBillRequest options) {
        if (options.getClientOverride() != null) {
            return options.getClientOverride();
        }
        Supplier supplier = resolveSupplier(tx);
        if (supplier != null) {
            BillPartyDto client = toClientParty(supplier);
            if (client != null) {
                return client;
            }
        }
        BillPartyDto client = new BillPartyDto();
        client.setDisplayName(firstNonBlank(tx.getVendorName(), "Client"));
        client.setAddress("N/A");
        return client;
    }

    private Supplier resolveSupplier(FinancialTransaction tx) {
        try {
            Supplier supplier = tx.getSupplier();
            UUID supplierId = extractSupplierId(supplier);
            if (supplierId == null) {
                return supplier;
            }
            return supplierRepository.findByIdAndIsDeletedFalse(supplierId)
                    .or(() -> supplierRepository.findById(supplierId))
                    .orElse(null);
        } catch (PersistenceException | IllegalStateException e) {
            return null;
        }
    }

    private BillPartyDto toClientParty(Supplier supplier) {
        try {
            BillPartyDto client = new BillPartyDto();
            client.setDisplayName(firstNonBlank(fullName(supplier), "Client"));
            client.setTaxRegistrationNumber(supplier.getMatriculeFiscal());
            client.setAddress(firstNonBlank(supplier.getAddress(), "N/A"));
            client.setPhone(supplier.getPhone());
            client.setEmail(supplier.getEmail());
            return client;
        } catch (PersistenceException | IllegalStateException e) {
            return null;
        }
    }

    private UUID extractSupplierId(Supplier supplier) {
        if (supplier == null) {
            return null;
        }
        Object id = entityManagerFactory.getPersistenceUnitUtil().getIdentifier(supplier);
        return id instanceof UUID uuid ? uuid : null;
    }

    private BigDecimal normalizedAmount(FinancialTransaction tx) {
        if (tx.getAmount() != null && tx.getAmount().signum() > 0) {
            return tx.getAmount();
        }
        return BigDecimal.ONE;
    }

    private String fallbackInvoiceNumber(FinancialTransaction tx) {
        if (tx.getId() != null) {
            return "INV-" + tx.getId();
        }
        return "INV-DRAFT";
    }

    private String fullName(Supplier supplier) {
        String first = supplier.getName() == null ? "" : supplier.getName().trim();
        String last = supplier.getLastname() == null ? "" : supplier.getLastname().trim();
        String full = (first + " " + last).trim();
        return full.isBlank() ? supplier.getEmail() : full;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
