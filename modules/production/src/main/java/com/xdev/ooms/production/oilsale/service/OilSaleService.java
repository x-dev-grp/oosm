package com.xdev.ooms.production.oilsale.service;

import com.xdev.ooms.production.oiltransaction.entity.OilTransaction;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;

import com.xdev.ooms.production.oilcontainer.entity.OilContainer;
import com.xdev.ooms.production.oilcontainer.repository.OilContainerRepository;
import com.xdev.ooms.production.oilcontainersale.entity.OilContainerSale;
import com.xdev.ooms.production.oilcontainersale.repository.OilContainerSaleRepo;
import com.xdev.ooms.production.oilsale.dto.OilContainerSaleLineDto;
import com.xdev.ooms.production.oilsale.dto.OilSaleCreateRequest;
import com.xdev.ooms.production.oilsale.dto.OilSaleDeliveryRequest;
import com.xdev.ooms.production.oilsale.dto.OilSaleDTO;
import com.xdev.ooms.production.oilsale.entity.OilSale;
import com.xdev.ooms.production.oilsale.repository.OilSaleRepository;
import com.xdev.ooms.production.oiltransaction.dto.OilTransactionDTO;
import com.xdev.ooms.production.oiltransaction.service.OilTransactionService;
import com.xdev.ooms.production.storageunit.entity.StorageUnit;
import com.xdev.ooms.production.storageunit.repository.StorageUnitRepo;
import com.xdev.ooms.production.supplier.entity.Supplier;
import com.xdev.ooms.production.supplier.repository.SupplierRepository;
import com.xdev.ooms.production.unifieddelivery.dto.PaymentDTO;



import  com.xdev.ooms.sharedkernel.Enum.*;
import com.xdev.ooms.sharedkernel.ports.FinancialTransactionPort;
import com.xdev.ooms.sharedkernel.ports.InvoiceNumberPort;
import com.xdev.ooms.sharedkernel.communicator.models.shared.FinancialTransactionDto;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.communicator.models.common.dtos.apiDTOs.models.SearchResponse;
import com.xdev.ooms.sharedkernel.models.SearchData;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import jakarta.validation.ValidationException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static  com.xdev.ooms.sharedkernel.Enum.OperationType.OIL_SALE_PAYMENT;

/**
 * Minimal service that:
 * - validates/locks inventory
 * - persists OilSale + container lines
 * - adjusts inventory
 * - creates settlement FinancialTransaction entries for cash received
 */
@Service
public class OilSaleService extends BaseServiceImpl<OilSale, OilSaleDTO, OilSaleDTO> {

    private final OilSaleRepository oilSaleRepository;

    private final StorageUnitRepo storageUnitRepo;
    private final OilContainerRepository containerRepo;
    private final OilContainerSaleRepo lineRepo;
    private final SupplierRepository supplierRepo;
    private final FinancialTransactionPort financialTransactionPort;
    private final ModelMapper modelMapper;
    private final OilContainerRepository oilContainerRepository;
    private final OilTransactionService oilTransactionService;
    private final InvoiceNumberPort invoiceNumberPort;

    public OilSaleService(OilSaleRepository oilSaleRepository, StorageUnitRepo storageUnitRepo, OilContainerRepository containerRepo, OilContainerSaleRepo lineRepo, SupplierRepository supplierRepo, FinancialTransactionPort financialTransactionPort, ModelMapper modelMapper, OilContainerRepository oilContainerRepository, OilTransactionService oilTransactionService, InvoiceNumberPort invoiceNumberPort) {
        super(oilSaleRepository, modelMapper);

        this.oilSaleRepository = oilSaleRepository;
        this.storageUnitRepo = storageUnitRepo;
        this.containerRepo = containerRepo;
        this.lineRepo = lineRepo;
        this.supplierRepo = supplierRepo;
        this.financialTransactionPort = financialTransactionPort;
        this.modelMapper = modelMapper;
        this.oilContainerRepository = oilContainerRepository;
        this.oilTransactionService = oilTransactionService;
        this.invoiceNumberPort = invoiceNumberPort;
    }

    @Override
    @Transactional(readOnly = true)
    public OilSaleDTO findById(UUID id) {
        OilSaleDTO dto = super.findById(id);
        dto.setContainerSales(loadContainerSaleLines(id));
        return dto;
    }

    private List<OilContainerSaleLineDto> loadContainerSaleLines(UUID saleId) {
        return lineRepo.findByOilSaleId(saleId).stream()
                .map(line -> {
                    OilContainerSaleLineDto dto = new OilContainerSaleLineDto();
                    dto.setId(line.getId());
                    if (line.getContainer() != null) {
                        dto.setContainerId(line.getContainer().getId());
                        dto.setContainerName(line.getContainer().getName());
                        dto.setCapacityInLiters(line.getContainer().getCapacityInLiters());
                    }
                    dto.setCount(line.getCount());
                    dto.setUnitPrice(line.getUnitPrice());
                    dto.setLineTotal(line.getLineTotal());
                    return dto;
                })
                .toList();
    }


    @Transactional
    public void processPayment(PaymentDTO paymentDTO) {
        if (paymentDTO.getIdOperation() == null) {
            throw new IllegalArgumentException("Payment operation id is required");
        }
        OilSale oilSale = oilSaleRepository.findByIdAndIsDeletedFalse(paymentDTO.getIdOperation()).orElse(null);
        if (oilSale == null) {
            throw new IllegalArgumentException("Oil Sale not found for ID: " + paymentDTO.getIdOperation());
        }
        BigDecimal paidAmount = oilSale.getPaidAmount() != null ? BigDecimal.valueOf(oilSale.getPaidAmount()) : BigDecimal.ZERO;
        BigDecimal unpaidAmount = oilSale.getUnpaidAmount() != null ? BigDecimal.valueOf(oilSale.getUnpaidAmount()) : BigDecimal.ZERO;

        double payment = paymentDTO.getAmount() != null ? paymentDTO.getAmount() : 0d;
        if (payment <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than 0");
        }
        if (unpaidAmount.signum() <= 0) {
            throw new IllegalArgumentException("Oil sale is already fully paid: " + oilSale.getId());
        }
        if (payment > unpaidAmount.doubleValue()) {
            payment = unpaidAmount.doubleValue();
        }

        oilSale.setPaid(payment > 0 && Math.abs(payment - unpaidAmount.doubleValue()) < 0.001);
        if (oilSale.isPaid()) {
            oilSale.setStatus(SaleStatus.DELIVERED);
        }
        oilSale.setPaidAmount((paidAmount.add(BigDecimal.valueOf(payment))).doubleValue());
        oilSale.setUnpaidAmount((unpaidAmount.subtract(BigDecimal.valueOf(payment))).doubleValue());

        oilSaleRepository.save(oilSale);
        FinancialTransactionDto financialTransactionDto = new FinancialTransactionDto();
        financialTransactionDto.setAmount(BigDecimal.valueOf(payment));
        financialTransactionDto.setTransactionType(TransactionType.OIL_SALE);
        financialTransactionDto.setApproved(true);
        financialTransactionDto.setCurrency(paymentDTO.getCurrency() != null ? paymentDTO.getCurrency() : Currency.TND);
        financialTransactionDto.setDirection(TransactionDirection.INBOUND);
        if (paymentDTO.getBankAccount() != null) {
            financialTransactionDto.setBankAccount(paymentDTO.getBankAccount());
        }
        if (paymentDTO.getCheckNumber() != null) {
            financialTransactionDto.setCheckNumber(paymentDTO.getCheckNumber());
        }
        financialTransactionDto.setTransactionDate(LocalDateTime.now());
        financialTransactionDto.setPaymentMethod(paymentDTO.getPaymentMethod() != null ? paymentDTO.getPaymentMethod() : PaymentMethod.CASH);
        financialTransactionDto.setLotNumber(null);
        financialTransactionDto.setsupplier(paymentDTO.getSupplier() != null ? modelMapper.map(paymentDTO.getSupplier(), com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto.class) : null);
        financialTransactionDto.setApprovalDate(LocalDateTime.now());
        financialTransactionDto.setOperationType(OIL_SALE_PAYMENT);
        financialTransactionDto.setExternalTransactionId(saleExternalReference(oilSale));
        financialTransactionDto.setResourceName(ResourceName.OILSALE);
        financialTransactionDto.setInvoiceReference(oilSale.getInvoiceNumber());
        financialTransactionDto.setDescription(buildPaymentDescription(oilSale));
        financialTransactionDto.setSyncProductionState(false);
        financialTransactionPort.record(financialTransactionDto);

    }

    @Transactional
    public OilSaleDTO createWithContainers(OilSaleCreateRequest req) {

        // ---- 0) Basic validation
        final boolean hasContainers = req.getContainerSales() != null && !req.getContainerSales().isEmpty();
        if (req.getQuantity() == null || (req.getQuantity().signum() <= 0 && !hasContainers)) {
            throw new ValidationException("quantity must be > 0 (or provide container lines)");
        }
        if (req.getQuantity() != null && req.getQuantity().signum() < 0) {
            throw new ValidationException("quantity must be >= 0");
        }
        if (req.getUnitPrice() == null || req.getUnitPrice().signum() < 0) {
            throw new ValidationException("unitPrice must be >= 0");
        }
        if (req.getSaleDate() == null) {
            throw new ValidationException("saleDate is required");
        }

        // Supplier is optional
        Supplier supplier = null;
        if (req.getSupplier() != null && !req.getSupplier().isBlank()) {
            supplier = supplierRepo.findByIdAndIsDeletedFalse(UUID.fromString(req.getSupplier())).orElse(null);
        }

        // ---- 1) Load storage unit & check existence
        StorageUnit su = storageUnitRepo.findByIdAndIsDeletedFalse(UUID.fromString(req.getStorageUnit())).orElse(null);
        if (su == null) {
            throw new IllegalArgumentException("Storage unit not found: " + req.getStorageUnit());
        }

        BigDecimal oilQty = req.getQuantity() != null ? req.getQuantity() : BigDecimal.ZERO;

        // ---- 2) Lock storage unit and check oil stock (only when selling oil)
        if (oilQty.signum() > 0) {
            BigDecimal suQty = BigDecimal.valueOf(su.getCurrentVolume());
            if (suQty.compareTo(oilQty) < 0) {
                throw new IllegalStateException("Insufficient oil in storage unit");
            }
        }

        // ---- 3) Branch on containers presence
        BigDecimal containerTotal = BigDecimal.ZERO;
        if (hasContainers) {
            // Compute total for containers only when list is provided
            for (var line : req.getContainerSales()) {
                OilContainer c = containerRepo.findById(line.getId()).orElseThrow(() -> new IllegalArgumentException("Container not found: " + line.getId()));
                BigDecimal lineTotal = c.getSellingPrice().multiply(BigDecimal.valueOf(line.getCount()));
                containerTotal = containerTotal.add(lineTotal);
            }
        }

        // ---- 4) Oil totals
        BigDecimal oilTotal = req.getUnitPrice().multiply(oilQty);
        BigDecimal saleTotal = oilTotal.add(containerTotal);

        BigDecimal paid = (req.getPaidAmount() == null) ? BigDecimal.ZERO : BigDecimal.valueOf(req.getPaidAmount());
        if (paid.signum() < 0) paid = BigDecimal.ZERO;
        if (paid.compareTo(saleTotal) > 0) paid = saleTotal;

        BigDecimal unpaid = saleTotal.subtract(paid);

        // ---- 5) Create sale
        OilSale sale = new OilSale();
        sale.setSupplier(supplier);
        sale.setStorageUnit(su.getId());
        sale.setQuantity(BigDecimal.valueOf(oilQty.doubleValue()));
        sale.setUnitPrice(BigDecimal.valueOf(req.getUnitPrice().doubleValue()));
        sale.setTotalAmount(BigDecimal.valueOf(saleTotal.doubleValue()));
        sale.setPaidAmount(paid.doubleValue());
        sale.setUnpaidAmount(unpaid.doubleValue());
        sale.setCurrency(req.getCurrency());
        sale.setPaymentMethod(req.getPaymentMethod());
        sale.setSaleDate(req.getSaleDate());
        sale.setQualityGrade(req.getQualityGrade());
        sale.setInvoiceNumber(resolveInvoiceNumber(req));
        sale.setDescription(req.getDescription());
        if (req.getDeliveryAddress() != null && !req.getDeliveryAddress().isBlank()) {
            sale.setDeliveryAddress(req.getDeliveryAddress().trim());
        }
        sale.setStatus(unpaid.signum() == 0 ? SaleStatus.CONFIRMED : SaleStatus.PENDING);

        sale = oilSaleRepository.save(sale);
        if (oilQty.signum() > 0) {
            OilTransactionDTO oiltTransactionDto = new OilTransactionDTO();
            oiltTransactionDto.setUnitPrice(req.getUnitPrice().doubleValue());
            oiltTransactionDto.setQuantityKg(oilQty.doubleValue());
            oiltTransactionDto.setTotalPrice(oilQty.doubleValue() * req.getUnitPrice().doubleValue());
            oiltTransactionDto.setOilSaleId(sale.getId());
            oilTransactionService.createOilTransactionForSale(oiltTransactionDto);
        }
        // ---- 6) Persist container lines (only if present)
        if (hasContainers) {
            for (OilContainerSale l : req.getContainerSales()) {
                OilContainer c = oilContainerRepository.findByIdAndIsDeletedFalse(l.getId()).orElseThrow(() -> new IllegalArgumentException("Container not found: " + l.getId()));

                OilContainerSale line = new OilContainerSale();
                line.setOilSale(sale);
                line.setContainer(c);
                line.setCount(l.getCount());
                line.setUnitPrice(c.getSellingPrice());
                line.setLineTotal(c.getSellingPrice().multiply(BigDecimal.valueOf(l.getCount())));
                lineRepo.save(line);

                c.setStockQuantity(c.getStockQuantity() - l.getCount());
                containerRepo.save(c);
            }
        }

        // Oil stock is deducted when the linked oil transaction is approved in storage (see createOilTransactionForSale).

        // ---- 7) Financial transactions (settlement-only: cash received, not full invoice revenue)
        if (paid.signum() > 0) {
            financialTransactionPort.record(buildSettlementTransaction(req, paid, supplier, sale));
        }

        sale = ensureQrCodeIfSupported(sale);
        return findById(sale.getId());
    }

    /**
     * Ledger cash received at sale creation. Full oil/container totals stay on the OilSale document
     * as unpaid until {@link #processPayment} records further settlements.
     */
    private FinancialTransactionDto buildSettlementTransaction(
            OilSaleCreateRequest req,
            BigDecimal paidAmount,
            Supplier supplier,
            OilSale sale) {
        FinancialTransactionDto tx = new FinancialTransactionDto();
        tx.setTransactionType(TransactionType.OIL_SALE);
        tx.setDirection(TransactionDirection.INBOUND);
        tx.setAmount(paidAmount);
        tx.setCurrency(req.getCurrency() != null ? req.getCurrency() : Currency.TND);
        tx.setPaymentMethod(req.getPaymentMethod() != null ? req.getPaymentMethod() : PaymentMethod.CASH);
        tx.setBankAccount(null);
        tx.setCheckNumber(null);
        tx.setLotNumber(null);
        if (supplier != null) {
            tx.setsupplier(modelMapper.map(supplier, com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto.class));
        }
        tx.setTransactionDate(LocalDateTime.now());
        tx.setApproved(true);
        tx.setApprovalDate(LocalDateTime.now());
        tx.setOperationType(OIL_SALE_PAYMENT);
        tx.setExternalTransactionId(saleExternalReference(sale));
        tx.setResourceName(ResourceName.OILSALE);
        tx.setInvoiceReference(sale.getInvoiceNumber());
        tx.setDescription(buildPaymentDescription(sale));
        tx.setSyncProductionState(false);
        return tx;
    }

    private String buildPaymentDescription(OilSale sale) {
        String invoiceRef = sale.getInvoiceNumber() != null && !sale.getInvoiceNumber().isBlank()
                ? sale.getInvoiceNumber().trim()
                : sale.getId().toString();
        if (!loadContainerSaleLines(sale.getId()).isEmpty()) {
            return "Paiement vente huile et conteneurs (" + invoiceRef + ")";
        }
        return "Paiement vente huile (" + invoiceRef + ")";
    }

    private String resolveInvoiceNumber(OilSaleCreateRequest req) {
        if (req.getInvoiceNumber() != null && !req.getInvoiceNumber().isBlank()) {
            return req.getInvoiceNumber().trim();
        }
        return invoiceNumberPort.nextInvoiceNumber();
    }

    private String saleExternalReference(OilSale sale) {
        return sale.getId().toString();
    }

    @Transactional
    public void cancelSale(UUID saleId) {
        // 1. Retrieve and validate the sale
        OilSale sale = oilSaleRepository.findByIdAndIsDeletedFalse(saleId).orElseThrow(() -> new IllegalArgumentException("Oil Sale not found: " + saleId));

        if (sale.getStatus() == SaleStatus.CANCELLED) {
            throw new IllegalStateException("Sale is already canceled: " + saleId);
        }

        // Optionally, restrict cancellation based on status
        if (sale.getStatus() == SaleStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel a delivered sale: " + saleId);
        }

        // 2. Cancel linked oil transaction and restore stock when it was already approved
        try {
            oilTransactionService.reverseOilTransactionForSale(sale.getId());
        } catch (IllegalArgumentException ex) {
            // No linked oil transaction — nothing to reverse in storage
        }

        // 3. Restore container stock (if applicable)
        List<OilContainerSale> containerSales = lineRepo.findByOilSaleId(saleId);
        for (OilContainerSale containerSale : containerSales) {
            OilContainer container = containerRepo.findByIdAndIsDeletedFalse(containerSale.getContainer().getId()).orElseThrow(() -> new IllegalArgumentException("Container not found: " + containerSale.getContainer().getId()));
            container.setStockQuantity(container.getStockQuantity() + containerSale.getCount());
            containerRepo.save(container);
        }

        // 4. Reverse all linked ledger rows (covers legacy full-invoice posts + settlements)
        financialTransactionPort.reverseLinked(saleExternalReference(sale), ResourceName.OILSALE);
        sale.setPaidAmount(0.0);
        sale.setUnpaidAmount(sale.getTotalAmount() != null ? sale.getTotalAmount().doubleValue() : 0.0);

        // 5. Update sale status
        sale.setStatus(SaleStatus.CANCELLED);
        sale.setDeleted(true);
        oilSaleRepository.save(sale);

        // 6. Delete container sale lines
        lineRepo.deleteAll(containerSales);
    }

    @Transactional
    public OilSaleDTO confirmSale(UUID saleId) {
        OilSale sale = oilSaleRepository.findByIdAndIsDeletedFalse(saleId)
                .orElseThrow(() -> new IllegalArgumentException("Oil Sale not found: " + saleId));
        if (sale.getStatus() == SaleStatus.CANCELLED) {
            throw new IllegalStateException("Cannot confirm a cancelled sale: " + saleId);
        }
        if (sale.getStatus() == SaleStatus.DELIVERED) {
            throw new IllegalStateException("Sale is already delivered: " + saleId);
        }
        if (sale.getStatus() != SaleStatus.PENDING) {
            throw new IllegalStateException("Only pending sales can be confirmed: " + saleId);
        }
        sale.setStatus(SaleStatus.CONFIRMED);
        return modelMapper.map(oilSaleRepository.save(sale), OilSaleDTO.class);
    }

    @Transactional
    public OilSaleDTO deliverSale(UUID saleId, OilSaleDeliveryRequest request) {
        OilSale sale = oilSaleRepository.findByIdAndIsDeletedFalse(saleId)
                .orElseThrow(() -> new IllegalArgumentException("Oil Sale not found: " + saleId));
        if (sale.getStatus() == SaleStatus.CANCELLED) {
            throw new IllegalStateException("Cannot deliver a cancelled sale: " + saleId);
        }
        if (sale.getStatus() == SaleStatus.DELIVERED) {
            throw new IllegalStateException("Sale is already delivered: " + saleId);
        }
        if (sale.getStatus() != SaleStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed sales can be delivered: " + saleId);
        }
        if (request != null) {
            if (request.getDeliveryDate() != null) {
                sale.setDeliveryDate(request.getDeliveryDate());
            }
            if (request.getDeliveryAddress() != null) {
                sale.setDeliveryAddress(request.getDeliveryAddress());
            }
            if (request.getDeliveryNotes() != null) {
                sale.setDeliveryNotes(request.getDeliveryNotes());
            }
        }
        sale.setStatus(SaleStatus.DELIVERED);
        if (sale.getDeliveryDate() == null) {
            sale.setDeliveryDate(LocalDateTime.now());
        }
        return modelMapper.map(oilSaleRepository.save(sale), OilSaleDTO.class);
    }

    @Transactional
    public OilSaleDTO cancelSaleAndReturn(UUID saleId) {
        cancelSale(saleId);
        return oilSaleRepository.findById(saleId)
                .map(s -> modelMapper.map(s, OilSaleDTO.class))
                .orElseThrow(() -> new IllegalArgumentException("Oil Sale not found after cancel: " + saleId));
    }

    @Override
    public Set<Action> actionsMapping(OilSale oilSale) {
        Set<Action> actions = new HashSet<>();
        actions.add(Action.READ);
        actions.add(Action.REGENERATE_QR);
        if (oilSale == null || Boolean.TRUE.equals(oilSale.getDeleted())) {
            return actions;
        }
        SaleStatus status = oilSale.getStatus();
        if (status == null || status == SaleStatus.CANCELLED) {
            return actions;
        }
        switch (status) {
            case PENDING -> {
                actions.add(Action.UPDATE);
                actions.add(Action.CONFIRM);
                actions.add(Action.CANCEL);
                actions.add(Action.GEN_PDF_BON_COMMANDE);
            }
            case CONFIRMED -> {
                actions.add(Action.UPDATE);
                actions.add(Action.DELIVER);
                actions.add(Action.CANCEL);
                actions.add(Action.GEN_PDF_BON_COMMANDE);
                actions.add(Action.GEN_INVOICE);
            }
            case DELIVERED -> {
                actions.add(Action.GEN_PDF_BON_COMMANDE);
                actions.add(Action.GEN_PDF_BON_LIVRAISON);
                actions.add(Action.GEN_INVOICE);
            }
            default -> {
            }
        }
        return actions;
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResponse<OilSale, OilSaleDTO> search(SearchData searchData) {
        return super.search(searchData);
    }

    @Override
    protected String getEntityType() {
        return "OILSALE";
    }

    @Override
    protected String getLabel(OilSale entity) {
        if (entity == null) {
            return "Oil sale";
        }
        if (entity.getInvoiceNumber() != null && !entity.getInvoiceNumber().isBlank()) {
            return entity.getInvoiceNumber();
        }
        return entity.getId() != null ? "Oil sale " + entity.getId() : "Oil sale";
    }

    @Override
    protected String getStatus(OilSale entity) {
        if (entity == null || entity.getStatus() == null) {
            return "UNKNOWN";
        }
        return entity.getStatus().name();
    }

    @Override
    protected String getMobileRoute() {
        return "/finance/oil-sales";
    }

    @Override
    protected String getWebRoute(OilSale entity) {
        if (entity == null || entity.getId() == null) {
            return "/finance/oil-sales";
        }
        return "/finance/oil-sales/" + entity.getId() + "/view";
    }
}