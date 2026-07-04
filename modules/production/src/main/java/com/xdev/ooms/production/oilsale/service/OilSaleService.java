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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static  com.xdev.ooms.sharedkernel.Enum.OperationType.OIL_CONTAINER_SALE;
import static  com.xdev.ooms.sharedkernel.Enum.OperationType.OIL_SALE;

/**
 * Minimal service that:
 * - validates/locks inventory
 * - persists OilSale + container lines
 * - adjusts inventory
 * - creates FinancialTransactionDto entries (revenue + optional payment)
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
            return;
        }
        OilSale oilSale = oilSaleRepository.findByIdAndIsDeletedFalse(paymentDTO.getIdOperation()).orElse(null);
        if (oilSale == null) {
            throw new IllegalArgumentException("Oil Sale not found for ID: " + paymentDTO.getIdOperation());
        }
        BigDecimal paidAmount = oilSale.getPaidAmount() != null ? BigDecimal.valueOf(oilSale.getPaidAmount()) : BigDecimal.ZERO;
        BigDecimal unpaidAmount = oilSale.getUnpaidAmount() != null ? BigDecimal.valueOf(oilSale.getUnpaidAmount()) : BigDecimal.ZERO;

        double payment = paymentDTO.getAmount() != null ? paymentDTO.getAmount() : 0d;

        oilSale.setPaid(payment > 0 && payment == unpaidAmount.doubleValue());
        if (payment > 0 && payment == unpaidAmount.doubleValue()) {
            oilSale.setStatus(SaleStatus.DELIVERED);
        }
        oilSale.setPaidAmount((paidAmount.add(BigDecimal.valueOf(payment))).doubleValue());
        oilSale.setUnpaidAmount((unpaidAmount.subtract(BigDecimal.valueOf(payment))).doubleValue());

        oilSaleRepository.save(oilSale);
        FinancialTransactionDto financialTransactionDto = new FinancialTransactionDto();
        financialTransactionDto.setAmount(BigDecimal.valueOf(paymentDTO.getAmount()));
        financialTransactionDto.setTransactionType(TransactionType.OIL_SALE);
        financialTransactionDto.setApproved(true);
        financialTransactionDto.setCurrency(paymentDTO.getCurrency());
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
        financialTransactionDto.setOperationType(OIL_SALE);
        financialTransactionDto.setExternalTransactionId(oilSale.getId().toString());
        financialTransactionDto.setResourceName(ResourceName.OILSALE);
        financialTransactionDto.setInvoiceReference(oilSale.getInvoiceNumber());
        financialTransactionDto.setDescription(buildPaymentDescription(oilSale));
        financialTransactionPort.record(financialTransactionDto);

    }

    @Transactional
    public OilSaleDTO createWithContainers(OilSaleCreateRequest req) {

        // ---- 0) Basic validation
        if (req.getQuantity() == null || req.getQuantity().signum() <= 0) {
            throw new ValidationException("quantity must be > 0");
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

        // ---- 2) Lock storage unit and check oil stock
        BigDecimal suQty = BigDecimal.valueOf(su.getCurrentVolume());
        if (suQty.compareTo(req.getQuantity()) < 0) {
            throw new IllegalStateException("Insufficient oil in storage unit");
        }

        // ---- 3) Branch on containers presence
        final boolean hasContainers = req.getContainerSales() != null && !req.getContainerSales().isEmpty();

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
        BigDecimal oilTotal = req.getUnitPrice().multiply(req.getQuantity());
        BigDecimal saleTotal = oilTotal.add(containerTotal);

        BigDecimal paid = (req.getPaidAmount() == null) ? BigDecimal.ZERO : BigDecimal.valueOf(req.getPaidAmount());
        if (paid.signum() < 0) paid = BigDecimal.ZERO;
        if (paid.compareTo(saleTotal) > 0) paid = saleTotal;

        BigDecimal unpaid = saleTotal.subtract(paid);

        // ---- 5) Create sale
        OilSale sale = new OilSale();
        sale.setSupplier(supplier);
        sale.setStorageUnit(su.getId());
        sale.setQuantity(BigDecimal.valueOf(req.getQuantity().doubleValue()));
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
        OilTransactionDTO oiltTransactionDto = new OilTransactionDTO();
        oiltTransactionDto.setUnitPrice(req.getUnitPrice().doubleValue());
        oiltTransactionDto.setQuantityKg(req.getQuantity().doubleValue());
        oiltTransactionDto.setTotalPrice(req.getQuantity().doubleValue() * req.getUnitPrice().doubleValue());
        oiltTransactionDto.setOilSaleId(sale.getId());
        oilTransactionService.createOilTransactionForSale(oiltTransactionDto);
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

        // ---- 7) Financial transactions
        // Revenue: OIL
        if (oilTotal.signum() > 0) {
            FinancialTransactionDto financialTransactionDto = getFinancialTransactionDto(req, oilTotal, supplier, sale);
            financialTransactionPort.record(financialTransactionDto);
        }

        // Revenue: CONTAINERS (only if we had container lines)
        if (hasContainers && containerTotal.signum() > 0) {
            FinancialTransactionDto financialTransactionDto = getTransactionDto(req, containerTotal, supplier, sale);
            financialTransactionPort.record(financialTransactionDto);
        }


        return findById(sale.getId());
    }

    private FinancialTransactionDto getTransactionDto(OilSaleCreateRequest req, BigDecimal containerTotal, Supplier supplier, OilSale sale) {
        FinancialTransactionDto financialTransactionDto = new FinancialTransactionDto();
        financialTransactionDto.setTransactionType(TransactionType.OIL_CONTAINER_SALE);
        return getFinancialTransactionDto(req, containerTotal, supplier, sale, financialTransactionDto, OIL_CONTAINER_SALE);
    }

    private FinancialTransactionDto getFinancialTransactionDto(OilSaleCreateRequest req, BigDecimal containerTotal, Supplier supplier, OilSale sale, FinancialTransactionDto financialTransactionDto, OperationType operationType) {
        financialTransactionDto.setDirection(TransactionDirection.INBOUND);
        financialTransactionDto.setAmount(containerTotal);
        financialTransactionDto.setCurrency(req.getCurrency() != null ? req.getCurrency() : Currency.TND);
        financialTransactionDto.setPaymentMethod(req.getPaymentMethod() != null ? req.getPaymentMethod() : PaymentMethod.CASH);
        financialTransactionDto.setBankAccount(null);
        financialTransactionDto.setCheckNumber(null);
        financialTransactionDto.setLotNumber(null);
        if (supplier != null) {
            financialTransactionDto.setsupplier(modelMapper.map(supplier, com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto.class));
        }
        financialTransactionDto.setTransactionDate(LocalDateTime.now());
        financialTransactionDto.setApproved(true);
        financialTransactionDto.setApprovalDate(LocalDateTime.now());
        financialTransactionDto.setOperationType(operationType);
        financialTransactionDto.setExternalTransactionId(saleExternalReference(sale));
        financialTransactionDto.setResourceName(ResourceName.OILSALE);
        financialTransactionDto.setInvoiceReference(sale.getInvoiceNumber());
        financialTransactionDto.setDescription(buildSaleTransactionDescription(sale, financialTransactionDto.getTransactionType()));
        return financialTransactionDto;
    }

    private String buildSaleTransactionDescription(OilSale sale, TransactionType transactionType) {
        String invoiceRef = sale.getInvoiceNumber() != null && !sale.getInvoiceNumber().isBlank()
                ? sale.getInvoiceNumber().trim()
                : sale.getId().toString();
        if (transactionType == TransactionType.OIL_CONTAINER_SALE) {
            String containers = loadContainerSaleLines(sale.getId()).stream()
                    .filter(line -> line.getCount() != null && line.getCount() > 0)
                    .map(line -> line.getCount() + " x " + (line.getContainerName() != null ? line.getContainerName() : "Conteneur"))
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Conteneurs");
            return "Vente conteneurs (" + invoiceRef + ") - " + containers;
        }
        return "Vente huile (" + invoiceRef + ")";
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

    private FinancialTransactionDto buildReversalTransaction(
            OilSale sale,
            BigDecimal amount,
            TransactionType transactionType,
            OperationType operationType) {
        FinancialTransactionDto reversal = new FinancialTransactionDto();
        reversal.setTransactionType(transactionType);
        reversal.setDirection(TransactionDirection.OUTBOUND);
        reversal.setAmount(amount);
        reversal.setCurrency(sale.getCurrency() != null ? sale.getCurrency() : Currency.TND);
        reversal.setPaymentMethod(sale.getPaymentMethod() != null ? sale.getPaymentMethod() : PaymentMethod.CASH);
        reversal.setTransactionDate(LocalDateTime.now());
        reversal.setApproved(true);
        reversal.setApprovalDate(LocalDateTime.now());
        reversal.setOperationType(operationType);
        reversal.setExternalTransactionId(saleExternalReference(sale));
        reversal.setResourceName(ResourceName.OILSALE);
        if (sale.getSupplier() != null) {
            reversal.setsupplier(modelMapper.map(
                    sale.getSupplier(),
                    com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto.class));
        }
        return reversal;
    }

    private String saleExternalReference(OilSale sale) {
        return sale.getId().toString();
    }

    private BigDecimal oilLineTotal(OilSale sale) {
        if (sale.getQuantity() == null || sale.getUnitPrice() == null) {
            return BigDecimal.ZERO;
        }
        return sale.getQuantity().multiply(sale.getUnitPrice()).setScale(2, RoundingMode.HALF_UP);
    }

    private FinancialTransactionDto getFinancialTransactionDto(OilSaleCreateRequest req, BigDecimal oilTotal, Supplier supplier, OilSale sale) {
        FinancialTransactionDto financialTransactionDto = new FinancialTransactionDto();
        financialTransactionDto.setTransactionType(TransactionType.OIL_SALE);
        return getFinancialTransactionDto(req, oilTotal, supplier, sale, financialTransactionDto, OIL_SALE);
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

        // 4. Reverse financial transactions (positive amounts + OUTBOUND direction)
        BigDecimal oilTotal = oilLineTotal(sale);
        if (oilTotal.compareTo(BigDecimal.ZERO) > 0) {
            financialTransactionPort.record(buildReversalTransaction(
                    sale, oilTotal, TransactionType.OIL_SALE, OIL_SALE));
        }

        // Container sale transaction (if containers exist)
        if (!containerSales.isEmpty()) {
            BigDecimal containerTotal = containerSales.stream()
                    .map(OilContainerSale::getLineTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (containerTotal.compareTo(BigDecimal.ZERO) > 0) {
                financialTransactionPort.record(buildReversalTransaction(
                        sale, containerTotal, TransactionType.OIL_CONTAINER_SALE, OIL_CONTAINER_SALE));
            }
        }

        // 5. Reverse payments (if any)
        if (sale.getPaidAmount() != null && sale.getPaidAmount() > 0) {
            financialTransactionPort.record(buildReversalTransaction(
                    sale,
                    BigDecimal.valueOf(sale.getPaidAmount()),
                    TransactionType.OIL_SALE,
                    OIL_SALE));

            sale.setPaidAmount(0.0);
            sale.setUnpaidAmount(sale.getTotalAmount().doubleValue());
        }

        // 7. Update sale status
        sale.setStatus(SaleStatus.CANCELLED);
        sale.setDeleted(true); // Optional: soft-delete
        oilSaleRepository.save(sale);

        // 8. Delete container sale lines (optional, depending on requirements)
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
}