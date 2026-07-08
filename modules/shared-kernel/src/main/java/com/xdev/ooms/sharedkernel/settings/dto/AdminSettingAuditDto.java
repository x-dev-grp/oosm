package com.xdev.ooms.sharedkernel.settings.dto;

import java.time.LocalDateTime;

public class AdminSettingAuditDto {
    private String settingKey;
    private String action;
    private String oldValueMasked;
    private String newValueMasked;
    private String changedByUsername;
    private LocalDateTime changedAt;
    private String reason;
    private boolean success;
    private String failureReason;

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOldValueMasked() {
        return oldValueMasked;
    }

    public void setOldValueMasked(String oldValueMasked) {
        this.oldValueMasked = oldValueMasked;
    }

    public String getNewValueMasked() {
        return newValueMasked;
    }

    public void setNewValueMasked(String newValueMasked) {
        this.newValueMasked = newValueMasked;
    }

    public String getChangedByUsername() {
        return changedByUsername;
    }

    public void setChangedByUsername(String changedByUsername) {
        this.changedByUsername = changedByUsername;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
