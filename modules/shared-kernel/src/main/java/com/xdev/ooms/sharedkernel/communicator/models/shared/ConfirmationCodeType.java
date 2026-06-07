package com.xdev.ooms.sharedkernel.communicator.models.shared;

public enum ConfirmationCodeType {
    ACCOUNT_ACTIVATION(1),
    RESETPASSWORD(2);
    private final int value;

    ConfirmationCodeType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
