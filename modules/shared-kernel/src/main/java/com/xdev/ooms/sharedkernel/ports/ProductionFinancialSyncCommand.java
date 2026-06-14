package com.xdev.ooms.sharedkernel.ports;

import com.xdev.ooms.sharedkernel.Enum.ResourceName;
import com.xdev.ooms.sharedkernel.Enum.TransactionType;

import java.math.BigDecimal;

public record ProductionFinancialSyncCommand(
        String externalTransactionId,
        ResourceName resourceName,
        BigDecimal amount,
        String invoiceReference,
        TransactionType transactionType,
        String lotNumber
) {
}
