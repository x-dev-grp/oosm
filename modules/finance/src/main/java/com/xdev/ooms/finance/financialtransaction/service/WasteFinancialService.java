package com.xdev.ooms.finance.financialtransaction.service;

import com.xdev.ooms.finance.financialtransaction.dto.FinancialTransactionDto;
import com.xdev.ooms.sharedkernel.Enum.Currency;
import com.xdev.ooms.sharedkernel.Enum.PaymentMethod;
import com.xdev.ooms.sharedkernel.Enum.ResourceName;
import com.xdev.ooms.sharedkernel.Enum.TransactionType;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Waste financial entries go through {@link FinancialTransactionService} so validation,
 * invoice numbering, and relation resolution stay consistent with the rest of the ledger.
 */
@Service
public class WasteFinancialService {

    private final FinancialTransactionService financialTransactionService;

    public WasteFinancialService(FinancialTransactionService financialTransactionService) {
        this.financialTransactionService = financialTransactionService;
    }

    @Transactional
    public FinancialTransactionDto createWasteFinancialTransaction(FinancialTransactionDto transactionDto) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "createWasteFinancialTransaction", transactionDto);

        try {
            if (transactionDto.getTransactionDate() == null) {
                transactionDto.setTransactionDate(LocalDateTime.now());
            }
            if (transactionDto.getCurrency() == null) {
                transactionDto.setCurrency(Currency.TND);
            }
            if (transactionDto.getTransactionType() == null) {
                transactionDto.setTransactionType(TransactionType.WASTE_SALE);
            }
            if (transactionDto.getPaymentMethod() == null) {
                transactionDto.setPaymentMethod(PaymentMethod.CASH);
            }
            if (transactionDto.getResourceName() == null) {
                transactionDto.setResourceName(ResourceName.Waste);
            }
            if (transactionDto.getSyncProductionState() == null) {
                // Waste document balances are usually updated by production before this call.
                transactionDto.setSyncProductionState(false);
            }

            FinancialTransactionDto result = financialTransactionService.save(transactionDto);

            OOSMLogger.logBusinessEvent(this.getClass(), "WASTE_FINANCIAL_TRANSACTION_CREATED",
                    "Created waste financial transaction - type: " + result.getTransactionType()
                            + ", amount: " + result.getAmount()
                            + ", description: " + result.getDescription());

            OOSMLogger.logMethodExit(this.getClass(), "createWasteFinancialTransaction", result);
            OOSMLogger.logPerformance(this.getClass(), "createWasteFinancialTransaction", startTime, System.currentTimeMillis());

            return result;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(), "Error creating waste financial transaction", e);
            throw new RuntimeException("Failed to create waste financial transaction: " + e.getMessage(), e);
        }
    }
}
