package com.xdev.ooms.production.oilcontainer.service;


import com.xdev.ooms.production.oilcontainer.dto.OilContainerDTO;
import com.xdev.ooms.production.oilcontainer.dto.OilContainerPurchaseRequest;
import com.xdev.ooms.production.oilcontainer.dto.OilContainerPurchaseResult;
import com.xdev.ooms.production.oilcontainer.entity.OilContainer;
import com.xdev.ooms.inventory.materielsupplier.entity.MaterielSupplier;
import com.xdev.ooms.inventory.materielsupplier.repository.MaterielSupplierRepository;
import com.xdev.ooms.production.oilcontainer.repository.OilContainerRepository;
import com.xdev.ooms.sharedkernel.Enum.ExpenseCategory;
import com.xdev.ooms.sharedkernel.Enum.PaymentMethod;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.ports.ExpensePort;
import com.xdev.ooms.sharedkernel.ports.ExpenseRecordCommand;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class OilContainerService extends BaseServiceImpl<OilContainer, OilContainerDTO, OilContainerDTO> {
    // Container purchases use inventory MaterielSupplier — not production Supplier (olive/oil farmers).

    private final OilContainerRepository oilContainerRepository;
    private final MaterielSupplierRepository materielSupplierRepository;
    private final ExpensePort expensePort;
    private final ModelMapper modelMapper;

    public OilContainerService(
            OilContainerRepository repository,
            ModelMapper modelMapper,
            ExpensePort expensePort,
            MaterielSupplierRepository materielSupplierRepository) {
        super(repository, modelMapper);
        this.oilContainerRepository = repository;
        this.expensePort = expensePort;
        this.modelMapper = modelMapper;
        this.materielSupplierRepository = materielSupplierRepository;
    }

    @Override
    public Set<Action> actionsMapping(OilContainer container) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ));
        if (container != null && Boolean.TRUE.equals(container.getActive())) {
            actions.add(Action.ENTREE_STOCK);
        }
        return actions;
    }

    @Transactional
    public OilContainerPurchaseResult purchase(UUID containerId, OilContainerPurchaseRequest request) {
        if (request == null) {
            throw new ValidationException("Purchase payload is required");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new ValidationException("Quantity must be greater than 0");
        }

        OilContainer container = oilContainerRepository.findByIdAndIsDeletedFalse(containerId)
                .orElseThrow(() -> new EntityNotFoundException("Oil container not found: " + containerId));
        if (!Boolean.TRUE.equals(container.getActive())) {
            throw new IllegalStateException("Cannot purchase an inactive container");
        }

        BigDecimal unitPrice = resolveUnitPrice(container, request.getUnitPrice());
        if (unitPrice.signum() <= 0) {
            throw new ValidationException("Unit price must be greater than 0");
        }

        int previousStock = container.getStockQuantity() == null ? 0 : container.getStockQuantity();
        int purchasedQty = request.getQuantity();
        BigDecimal totalAmount = unitPrice
                .multiply(BigDecimal.valueOf(purchasedQty))
                .setScale(2, RoundingMode.HALF_UP);

        container.setStockQuantity(previousStock + purchasedQty);
        container.setBuyPrice(unitPrice);
        OilContainer saved = oilContainerRepository.save(container);

        String vendor = resolveVendor(request);

        PaymentMethod paymentMethod = request.getPaymentMethod() != null
                ? request.getPaymentMethod()
                : PaymentMethod.CASH;

        String purchaseLabel = saved.getName() + " x" + purchasedQty;
        String notes = buildNotes(request.getNotes(), purchaseLabel, previousStock, saved.getStockQuantity());

        String invoiceReference = expensePort.record(new ExpenseRecordCommand(
                vendor,
                totalAmount.doubleValue(),
                ExpenseCategory.PACKAGING_CONTAINERS,
                paymentMethod,
                "Achat conteneur d'huile",
                purchaseLabel,
                notes,
                saved.getId().toString()));

        OilContainerPurchaseResult result = new OilContainerPurchaseResult();
        result.setContainer(modelMapper.map(saved, OilContainerDTO.class));
        result.setPurchasedQuantity(purchasedQty);
        result.setUnitPrice(unitPrice);
        result.setTotalAmount(totalAmount);
        result.setInvoiceReference(invoiceReference);
        result.setPreviousStockQuantity(previousStock);
        result.setNewStockQuantity(saved.getStockQuantity());
        return result;
    }

    private BigDecimal resolveUnitPrice(OilContainer container, BigDecimal requestedUnitPrice) {
        if (requestedUnitPrice != null && requestedUnitPrice.signum() > 0) {
            return requestedUnitPrice.setScale(2, RoundingMode.HALF_UP);
        }
        if (container.getBuyPrice() != null && container.getBuyPrice().signum() > 0) {
            return container.getBuyPrice().setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private String resolveVendor(OilContainerPurchaseRequest request) {
        if (request.getMaterielSupplierId() != null) {
            MaterielSupplier supplier = materielSupplierRepository.findByIdAndIsDeletedFalse(request.getMaterielSupplierId())
                    .orElseThrow(() -> new ValidationException("Material supplier not found: " + request.getMaterielSupplierId()));
            return supplier.displayName();
        }

        String vendor = request.getVendor() == null ? "" : request.getVendor().trim();
        if (vendor.isBlank()) {
            throw new ValidationException("Material supplier is required");
        }
        return vendor;
    }

    private String buildNotes(String userNotes, String purchaseLabel, int previousStock, int newStock) {
        String stockNote = "Stock: " + previousStock + " -> " + newStock;
        if (userNotes == null || userNotes.isBlank()) {
            return purchaseLabel + " | " + stockNote;
        }
        return userNotes.trim() + " | " + purchaseLabel + " | " + stockNote;
    }

    @Override
    protected String getEntityType() {
        return "OILCONTAINER";
    }

    @Override
    protected String getLabel(OilContainer entity) {
        if (entity == null) {
            return "Oil container";
        }
        if (entity.getName() != null && !entity.getName().isBlank()) {
            return entity.getName();
        }
        return entity.getId() != null ? "Oil container " + entity.getId() : "Oil container";
    }

    @Override
    protected String getStatus(OilContainer entity) {
        if (entity == null) {
            return "UNKNOWN";
        }
        return Boolean.TRUE.equals(entity.getActive()) ? "ACTIVE" : "INACTIVE";
    }

    @Override
    protected String getMobileRoute() {
        return "/oil-containers";
    }

    @Override
    protected String getWebRoute(OilContainer entity) {
        if (entity == null || entity.getId() == null) {
            return "/oil-containers";
        }
        return "/oil-containers/" + entity.getId();
    }
}
