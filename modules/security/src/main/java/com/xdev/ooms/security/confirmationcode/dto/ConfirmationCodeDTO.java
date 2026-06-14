package com.xdev.ooms.security.confirmationcode.dto;

import com.xdev.ooms.security.confirmationcode.entity.ConfirmationCode;
import com.xdev.ooms.security.confirmationcode.enums.ConfirmationCodeType;
import com.xdev.ooms.sharedkernel.communicator.models.shared.OSMUserDTO;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.time.LocalDateTime;

public class ConfirmationCodeDTO extends BaseDto<ConfirmationCode> {
    private String code;
    private ConfirmationCodeType confirmationCodeType;
    private OSMUserDTO user;
    private int failedAttempts;
    private LocalDateTime consumedAt;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ConfirmationCodeType getConfirmationCodeType() {
        return confirmationCodeType;
    }

    public void setConfirmationCodeType(ConfirmationCodeType confirmationCodeType) {
        this.confirmationCodeType = confirmationCodeType;
    }

    public OSMUserDTO getUser() {
        return user;
    }

    public void setUser(OSMUserDTO user) {
        this.user = user;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public LocalDateTime getConsumedAt() {
        return consumedAt;
    }

    public void setConsumedAt(LocalDateTime consumedAt) {
        this.consumedAt = consumedAt;
    }
}
