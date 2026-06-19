package com.xdev.ooms.sharedkernel.ports;

import com.xdev.ooms.sharedkernel.Enum.ExpenseCategory;
import com.xdev.ooms.sharedkernel.Enum.PaymentMethod;

/**
 * Cross-module command to record a purchase expense (creates expense + financial transaction).
 */
public record ExpenseRecordCommand(
        String vendor,
        Double amount,
        ExpenseCategory category,
        PaymentMethod paymentMethod,
        String object,
        String purchaseNature,
        String notes,
        String externalReference) {
}
