package com.xdev.ooms.sharedkernel.ports;

import com.xdev.ooms.sharedkernel.Enum.OperationType;

import java.math.BigDecimal;

public record UnifiedDeliveryBillLineDto(
        BigDecimal quantity,
        String unit,
        BigDecimal unitPriceExcludingVat,
        BigDecimal totalExcludingVat,
        OperationType operationType,
        boolean oilDelivery) {
}
