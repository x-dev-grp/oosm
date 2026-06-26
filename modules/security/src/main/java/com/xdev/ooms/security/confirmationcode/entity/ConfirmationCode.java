package com.xdev.ooms.security.confirmationcode.entity;

import com.xdev.ooms.security.user.entity.OOSMUser;
import com.xdev.ooms.security.confirmationcode.enums.ConfirmationCodeType;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class ConfirmationCode extends BaseEntity {
    private String code;
    private ConfirmationCodeType confirmationCodeType;
    private int failedAttempts;
    private LocalDateTime consumedAt;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private OOSMUser user;

    public boolean isExpired() {
        return this.getLastModifiedDate().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    public boolean isConsumed() {
        return consumedAt != null;
    }


    public String getCode() {
        return code;
    }

    public ConfirmationCodeType getConfirmationCodeType() {
        return confirmationCodeType;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public LocalDateTime getConsumedAt() {
        return consumedAt;
    }

    public OOSMUser getUser() {
        return user;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setConfirmationCodeType(ConfirmationCodeType confirmationCodeType) {
        this.confirmationCodeType = confirmationCodeType;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public void setConsumedAt(LocalDateTime consumedAt) {
        this.consumedAt = consumedAt;
    }

    public void setUser(OOSMUser user) {
        this.user = user;
    }
}
