package com.xdev.ooms.finance.financialtransaction.service;

import com.xdev.ooms.finance.financialtransaction.dto.FinancialTransactionDto;
import com.xdev.ooms.finance.financialtransaction.dto.SupplierFinancialSummaryDto;
import com.xdev.ooms.finance.internal.FinanceModuleDtoMapper;
import com.xdev.ooms.finance.expense.entity.Expense;
import com.xdev.ooms.finance.financialtransaction.entity.FinancialTransaction;
import com.xdev.ooms.finance.expense.repository.ExpensesRepository;
import com.xdev.ooms.finance.financialtransaction.repository.FinancialTransactionRepository;
import com.xdev.ooms.sharedkernel.Enum.Currency;
import com.xdev.ooms.sharedkernel.Enum.ExpenseStatus;
import com.xdev.ooms.sharedkernel.Enum.ResourceName;
import com.xdev.ooms.sharedkernel.Enum.TransactionDirection;
import com.xdev.ooms.sharedkernel.Enum.TransactionType;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.ports.ProductionFinancialSyncCommand;
import com.xdev.ooms.sharedkernel.ports.ProductionFinancialSyncPort;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.utils.OSMLogger;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FinancialTransactionService extends BaseServiceImpl<FinancialTransaction, FinancialTransactionDto, FinancialTransactionDto> {

    private final FinancialTransactionRepository financialTransactionRepository;
    private final ProductionFinancialSyncPort productionFinancialSyncPort;
    private final ExpensesRepository expensesRepository;
    private final FinanceModuleDtoMapper moduleDtoMapper;

    public FinancialTransactionService(
            BaseRepository<FinancialTransaction> repository,
            ModelMapper modelMapper,
            FinancialTransactionRepository financialTransactionRepository,
            ProductionFinancialSyncPort productionFinancialSyncPort,
            ExpensesRepository expensesRepository,
            FinanceModuleDtoMapper moduleDtoMapper) {
        super(repository, modelMapper);
        this.financialTransactionRepository = financialTransactionRepository;
        this.productionFinancialSyncPort = productionFinancialSyncPort;
        this.expensesRepository = expensesRepository;
        this.moduleDtoMapper = moduleDtoMapper;
    }

    @Transactional
    public com.xdev.ooms.sharedkernel.communicator.models.shared.FinancialTransactionDto recordShared(
            com.xdev.ooms.sharedkernel.communicator.models.shared.FinancialTransactionDto request) {
        FinancialTransactionDto saved = save(moduleDtoMapper.fromShared(request));
        return moduleDtoMapper.toShared(saved);
    }

    @Override
    @Transactional
    public FinancialTransactionDto save(FinancialTransactionDto request) {
        validateRequest(request);

        if (request.getInvoiceReference() == null || request.getInvoiceReference().isBlank()) {
            request.setInvoiceReference(generateNextInvoiceRef());
        }

        boolean syncProduction = request.getSyncProductionState() == null
                || Boolean.TRUE.equals(request.getSyncProductionState());

        FinancialTransaction tx = modelMapper.map(request, FinancialTransaction.class);

        if (tx.getTransactionDate() == null) {
            tx.setTransactionDate(LocalDateTime.now());
        }
        if (tx.getCurrency() == null) {
            tx.setCurrency(Currency.TND);
        }
        if (tx.getDirection() == null) {
            tx.setDirection(inferDirection(tx.getTransactionType()));
        }

        FinancialTransaction savedTx = financialTransactionRepository.save(tx);

        if (syncProduction) {
            applyPostSaveEffects(savedTx);
        }

        return modelMapper.map(savedTx, FinancialTransactionDto.class);
    }

    @Override
    @Transactional
    public FinancialTransactionDto update(FinancialTransactionDto request) {
        validateRequest(request);
        FinancialTransactionDto updated = super.update(request);
        if (updated == null) {
            throw new jakarta.persistence.EntityNotFoundException("Financial transaction not found with id " + request.getId());
        }
        return updated;
    }

    @Transactional
    public FinancialTransactionDto approve(UUID id, String approvedBy) {
        FinancialTransaction tx = financialTransactionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Financial transaction not found with id " + id));
        tx.setApproved(Boolean.TRUE);
        tx.setApprovalDate(LocalDateTime.now());
        tx.setApprovedBy(approvedBy == null || approvedBy.isBlank() ? "System" : approvedBy);
        return modelMapper.map(financialTransactionRepository.save(tx), FinancialTransactionDto.class);
    }

    @Transactional
    public FinancialTransactionDto reject(UUID id, String rejectedBy, String reason) {
        FinancialTransaction tx = financialTransactionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Financial transaction not found with id " + id));
        tx.setApproved(Boolean.FALSE);
        tx.setApprovalDate(LocalDateTime.now());
        tx.setApprovedBy(rejectedBy == null || rejectedBy.isBlank() ? "System" : rejectedBy);
        if (reason != null && !reason.isBlank()) {
            String prefix = tx.getDescription() == null || tx.getDescription().isBlank()
                    ? ""
                    : tx.getDescription() + System.lineSeparator();
            tx.setDescription(prefix + "Rejection reason: " + reason);
        }
        return modelMapper.map(financialTransactionRepository.save(tx), FinancialTransactionDto.class);
    }

    private void validateRequest(FinancialTransactionDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Financial transaction payload is required");
        }
        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("Invalid amount: must be greater than 0");
        }
        if (request.getPaymentMethod() == null) {
            throw new IllegalArgumentException("Payment method is required");
        }
        if (request.getTransactionType() == null) {
            throw new IllegalArgumentException("Transaction type is required");
        }
    }

    private void applyPostSaveEffects(FinancialTransaction savedTx) {
        switch (savedTx.getTransactionType()) {
            case OIL_SALE, OIL_PURCHASE, PAYMENT, PURCHASE -> updateOilInvoice(savedTx);
            case WASTE_SALE, WASTE_PAYMENT -> updateWasteInvoice(savedTx);
            case SUPPLIER_PAYMENT, SUPPLIER_CREDIT -> updateSupplierInvoice(savedTx);
            case EXPENSE -> markExpensePaid(savedTx);
            default -> {
            }
        }
    }

    private String generateNextInvoiceRef() {
        return generateBusinessCode("invoiceReference", "INV");
    }

    private TransactionDirection inferDirection(TransactionType type) {
        return switch (type) {
            case PAYMENT, SUPPLIER_PAYMENT, OIL_PURCHASE, WITHDRAWAL, CHECK_PAYMENT, WASTE_DISPOSAL_COST ->
                    TransactionDirection.OUTBOUND;
            case CREDIT, SUPPLIER_CREDIT, OIL_SALE, DEPOSIT, CHECK_DEPOSIT, WASTE_SALE, WASTE_PAYMENT, STORAGE_RENTAL ->
                    TransactionDirection.INBOUND;
            case INTERNAL_TRANSFER -> TransactionDirection.INTERNAL;
            default -> TransactionDirection.INTERNAL;
        };
    }

    private void updateWasteInvoice(FinancialTransaction tx) {
        productionFinancialSyncPort.syncFromFinancialTransaction(toSyncCommand(tx));
        OSMLogger.logBusinessEvent(this.getClass(), "WASTE_INVOICE_UPDATE",
                "Synced waste invoice for transaction: " + tx.getId()
                        + ", invoice: " + tx.getInvoiceReference());
    }

    private void updateOilInvoice(FinancialTransaction tx) {
        if (tx.getResourceName() == null) {
            return;
        }
        productionFinancialSyncPort.syncFromFinancialTransaction(toSyncCommand(tx));
        OSMLogger.logBusinessEvent(this.getClass(), "OIL_INVOICE_UPDATE",
                "Synced production invoice for transaction: " + tx.getId()
                        + ", resource: " + tx.getResourceName());
    }

    private void updateSupplierInvoice(FinancialTransaction tx) {
        if (!hasDeliveryReference(tx)) {
            OSMLogger.logBusinessEvent(this.getClass(), "SUPPLIER_INVOICE_SYNC_SKIPPED",
                    "No delivery reference for supplier transaction: " + tx.getId());
            return;
        }
        productionFinancialSyncPort.syncFromFinancialTransaction(toSyncCommand(tx));
        OSMLogger.logBusinessEvent(this.getClass(), "SUPPLIER_INVOICE_UPDATE",
                "Synced supplier invoice for transaction: " + tx.getId()
                        + ", invoice: " + tx.getInvoiceReference()
                        + ", lot: " + tx.getLotNumber());
    }

    private boolean hasDeliveryReference(FinancialTransaction tx) {
        return (tx.getExternalTransactionId() != null && !tx.getExternalTransactionId().isBlank())
                || (tx.getLotNumber() != null && !tx.getLotNumber().isBlank());
    }

    private void markExpensePaid(FinancialTransaction tx) {
        Expense expense = tx.getExpense();
        if (expense == null || expense.getId() == null) {
            return;
        }
        expensesRepository.findById(expense.getId()).ifPresent(savedExpense -> {
            savedExpense.setStatus(ExpenseStatus.PAID);
            expensesRepository.save(savedExpense);
        });
    }

    private ProductionFinancialSyncCommand toSyncCommand(FinancialTransaction tx) {
        ResourceName resourceName = tx.getResourceName();
        if (resourceName == null
                && (tx.getTransactionType() == TransactionType.SUPPLIER_PAYMENT
                || tx.getTransactionType() == TransactionType.SUPPLIER_CREDIT)) {
            resourceName = ResourceName.UnifiedDelivery;
        }
        return new ProductionFinancialSyncCommand(
                tx.getExternalTransactionId(),
                resourceName,
                tx.getAmount(),
                tx.getInvoiceReference(),
                tx.getTransactionType(),
                tx.getLotNumber());
    }

    public List<FinancialTransactionDto> findBySupplierId(UUID supplierId) {
        return financialTransactionRepository.findBySupplier_IdAndIsDeletedFalseOrderByTransactionDateDesc(supplierId)
                .stream()
                .map(tx -> modelMapper.map(tx, FinancialTransactionDto.class))
                .collect(Collectors.toList());
    }

    public SupplierFinancialSummaryDto getSupplierFinancialSummary(UUID supplierId) {
        List<FinancialTransaction> transactions =
                financialTransactionRepository.findBySupplier_IdAndIsDeletedFalseOrderByTransactionDateDesc(supplierId);

        SupplierFinancialSummaryDto summary = new SupplierFinancialSummaryDto();
        summary.setSupplierId(supplierId);
        summary.setTransactionCount(transactions.size());

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalUnpaid = BigDecimal.ZERO;
        BigDecimal inboundAmount = BigDecimal.ZERO;
        BigDecimal outboundAmount = BigDecimal.ZERO;
        long inboundCount = 0;
        long outboundCount = 0;

        for (FinancialTransaction tx : transactions) {
            BigDecimal amount = tx.getAmount() != null ? tx.getAmount() : BigDecimal.ZERO;
            totalAmount = totalAmount.add(amount);

            if (tx.getPaidAmount() != null) {
                totalPaid = totalPaid.add(BigDecimal.valueOf(tx.getPaidAmount()));
            }
            if (tx.getUnpaidAmount() != null) {
                totalUnpaid = totalUnpaid.add(BigDecimal.valueOf(tx.getUnpaidAmount()));
            }

            if (tx.getDirection() == TransactionDirection.INBOUND) {
                inboundAmount = inboundAmount.add(amount);
                inboundCount++;
            } else if (tx.getDirection() == TransactionDirection.OUTBOUND) {
                outboundAmount = outboundAmount.add(amount);
                outboundCount++;
            }
        }

        summary.setTotalAmount(totalAmount);
        summary.setTotalPaidAmount(totalPaid);
        summary.setTotalUnpaidAmount(totalUnpaid);
        summary.setInboundAmount(inboundAmount);
        summary.setOutboundAmount(outboundAmount);
        summary.setInboundCount(inboundCount);
        summary.setOutboundCount(outboundCount);
        return summary;
    }

    @Override
    public Set<Action> actionsMapping(FinancialTransaction financialTransaction) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ, Action.APPROVE, Action.REJECT));
        return actions;
    }
}
