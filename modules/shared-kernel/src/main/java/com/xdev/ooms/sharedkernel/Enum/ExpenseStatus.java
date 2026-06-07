package com.xdev.ooms.sharedkernel.Enum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ExpenseStatus {
    PENDING, PAID, REIMBURSED;

    @JsonCreator
    public static ExpenseStatus fromString(String value) {
        if (value == null) return null;
        return ExpenseStatus.valueOf(value.trim().toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
} 