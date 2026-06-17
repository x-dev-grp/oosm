package com.xdev.ooms.sharedkernel.ports;

import com.xdev.ooms.sharedkernel.Enum.OperationType;

import java.math.BigDecimal;

public record UnifiedDeliveryBillLineQuery(
        String externalTransactionId,
        String lotNumber,
        OperationType operationType,
        BigDecimal fallbackTotalHt) {
}
