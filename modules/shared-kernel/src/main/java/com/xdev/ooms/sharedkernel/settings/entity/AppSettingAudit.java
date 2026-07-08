package com.xdev.ooms.sharedkernel.settings.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "app_setting_audit")
public class AppSettingAudit {

    @Id
    private UUID id;

    @Column(name = "setting_key", nullable = false, length = 150)
    private String settingKey;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(name = "old_value_masked", columnDefinition = "TEXT")
    private String oldValueMasked;

    @Column(name = "new_value_masked", columnDefinition = "TEXT")
    private String newValueMasked;

    @Column(name = "changed_by")
    private UUID changedBy;

    @Column(name = "changed_by_username", length = 150)
    private String changedByUsername;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false)
    private boolean success;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (changedAt == null) {
            changedAt = LocalDateTime.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public UUID getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(UUID changedBy) {
        this.changedBy = changedBy;
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

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
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
