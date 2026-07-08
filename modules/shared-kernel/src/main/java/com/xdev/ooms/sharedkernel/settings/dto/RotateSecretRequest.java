package com.xdev.ooms.sharedkernel.settings.dto;

public class RotateSecretRequest {
    private String value;
    private String reason;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
