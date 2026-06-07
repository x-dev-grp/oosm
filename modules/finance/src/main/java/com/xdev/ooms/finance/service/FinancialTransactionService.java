package com.xdev.ooms.finance.service;

import com.xdev.ooms.finance.dto.FinancialTransactionDto;
import com.xdev.ooms.finance.model.FinancialTransaction;
import com.xdev.ooms.finance.repo.FinancialTransactionRepository;
import  com.xdev.ooms.sharedkernel.Enum.Currency;
import  com.xdev.ooms.sharedkernel.Enum.TransactionDirection;
import  com.xdev.ooms.sharedkernel.Enum.TransactionType;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.utils.OSMLogger;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
public class FinancialTransactionService extends BaseServiceImpl<FinancialTransaction, FinancialTransactionDto, FinancialTransactionDto> {

    private final FinancialTransactionRepository financialTransactionRepository;
    public FinancialTransactionService(BaseRepository<FinancialTransaction> repository, ModelMapper modelMapper, FinancialTransactionRepository financialTransactionRepository) {
        super(repository, modelMapper);
        this.financialTransactionRepository = financialTransactionRepository;
     }


    @Override
    @Transactional
    public FinancialTransactionDto save(FinancialTransactionDto request) {

        // --- 0) Ensure invoiceReference ---
        if (request.getInvoiceReference() == null || request.getInvoiceReference().isBlank()) {
            request.setInvoiceReference(generateNextInvoiceRef());
        }

        // --- 1) Map → entity & defaults ---
        FinancialTransaction tx = modelMapper.map(request, FinancialTransaction.class);


        if (tx.getTransactionDate() == null) {
            tx.setTransactionDate(LocalDateTime.now());
        }
        if (tx.getCurrency() == null) {
            tx.setCurrency(Currency.TND);
        }
        // infer direction if not set
        if (tx.getDirection() == null) {
            tx.setDirection(inferDirection(tx.getTransactionType()));
        }

        FinancialTransaction savedTx = financialTransactionRepository.save(tx);
        switch (savedTx.getTransactionType()) {

            // — Oil sales & purchases update their invoice balance —
            case OIL_SALE, OIL_PURCHASE -> {
                if (savedTx.getInvoiceReference() != null) {
                    updateOilInvoice(savedTx);
                }
            }

            // — Waste sales & payments update waste records —
            case WASTE_SALE, WASTE_PAYMENT -> {
                if (savedTx.getInvoiceReference() != null) {
                    updateWasteInvoice(savedTx);
                }
            }

            // — Supplier payments/credits update supplier invoice balance —
            case SUPPLIER_PAYMENT, SUPPLIER_CREDIT -> {
                if (savedTx.getInvoiceReference() != null) {
                    updateSupplierInvoice(savedTx);
                }
            }

            // — Expense transactions mark the expense record paid —
            case EXPENSE -> {
                if (savedTx.getExpense() != null) {
                    markExpensePaid(savedTx);
                }
            }

            // — Other types (DEPOSIT, WITHDRAWAL, INTERNAL_TRANSFER, etc.) have no external side-effects —
            default -> {
                // no-op
            }
        }

        // --- 4) Map back → DTO & return ---
        return modelMapper.map(savedTx, FinancialTransactionDto.class);
    }

    private String generateNextInvoiceRef() {
        return generateBusinessCode("invoiceReference", "INV");
    }

    private TransactionDirection inferDirection(TransactionType type) {
        return switch (type) {
            case PAYMENT, SUPPLIER_PAYMENT, OIL_PURCHASE, WITHDRAWAL, CHECK_PAYMENT, WASTE_DISPOSAL_COST -> TransactionDirection.OUTBOUND;
            case CREDIT, SUPPLIER_CREDIT, OIL_SALE, DEPOSIT, CHECK_DEPOSIT, WASTE_SALE, WASTE_PAYMENT -> TransactionDirection.INBOUND;
            case INTERNAL_TRANSFER -> TransactionDirection.INTERNAL;
            default -> TransactionDirection.INTERNAL;
        };
    }

    private void updateWasteInvoice(FinancialTransaction tx) {
        // Implementation for waste invoice updates
        // This could integrate with waste management system
        // For now, just log the operation
        OSMLogger.logBusinessEvent(this.getClass(), "WASTE_INVOICE_UPDATE",
            "Updated waste invoice for transaction: " + tx.getId() + 
            ", invoice: " + tx.getInvoiceReference());
    }

    private void updateOilInvoice(FinancialTransaction tx) {

    }

    private void updateSupplierInvoice(FinancialTransaction tx) {

    }

    private void markExpensePaid(FinancialTransaction tx) {

    }


    @Override
    public Set<Action> actionsMapping(FinancialTransaction financialTransaction) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ));
        return actions;
    }


} 
