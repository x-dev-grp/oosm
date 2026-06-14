package com.xdev.ooms.security.confirmationcode.entity;

import com.xdev.ooms.security.user.entity.OSMUser;
import com.xdev.ooms.security.confirmationcode.enums.ConfirmationCodeType;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

@Entity
@Audited
public class ConfirmationCode extends BaseEntity {
    private String code;
    private ConfirmationCodeType confirmationCodeType;
    private int failedAttempts;
    private LocalDateTime consumedAt;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private OSMUser user;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isExpired() {
        return this.getLastModifiedDate().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    public boolean isConsumed() {
        return consumedAt != null;
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

    public ConfirmationCodeType getConfirmationCodeType() {
        return confirmationCodeType;
    }

    public void setConfirmationCodeType(ConfirmationCodeType confirmationCodeType) {
        this.confirmationCodeType = confirmationCodeType;
    }

    public OSMUser getUser() {
        return user;
    }

    public void setUser(OSMUser user) {
        this.user = user;
    }
}
