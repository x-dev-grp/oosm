package com.xdev.ooms.sharedkernel.settings.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateSettingRequest {
    private String value;
    private String reason;

    @JsonProperty("confirmRestart")
    private boolean confirmRestart;

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

    public boolean isConfirmRestart() {
        return confirmRestart;
    }

    public void setConfirmRestart(boolean confirmRestart) {
        this.confirmRestart = confirmRestart;
    }
}
