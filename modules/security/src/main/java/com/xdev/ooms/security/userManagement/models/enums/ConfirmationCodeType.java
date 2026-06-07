package com.xdev.ooms.security.userManagement.models.enums;

public enum ConfirmationCodeType {
    RESETPASSWORD(0);


    private final int value;

    ConfirmationCodeType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
