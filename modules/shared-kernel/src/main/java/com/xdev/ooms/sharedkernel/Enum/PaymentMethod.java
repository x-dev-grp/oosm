package com.xdev.ooms.sharedkernel.Enum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentMethod {
    CASH, CHEQUE, TRANSFER,OIL,MIXED;

    @JsonCreator
    public static PaymentMethod fromString(String value) {
        if (value == null) return null;
        return PaymentMethod.valueOf(value.trim().toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
} 