package com.xdev.ooms.production.dayimport.dto;

import java.time.Instant;

public class DayImportDriveStatusDto {
    private boolean enabled;
    private boolean configured;
    private String folderId;
    private String processedFolderId;
    private String lastSyncAt;
    private String lastResult;
    private String lastError;
    private String cron;
    private Integer pendingCount;
    private Integer processedCount;
    private Integer failedCount;
    private boolean oauthConfigured;
    private boolean connected;
    private String googleAccountEmail;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isConfigured() {
        return configured;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public String getProcessedFolderId() {
        return processedFolderId;
    }

    public void setProcessedFolderId(String processedFolderId) {
        this.processedFolderId = processedFolderId;
    }

    public String getLastSyncAt() {
        return lastSyncAt;
    }

    public void setLastSyncAt(String lastSyncAt) {
        this.lastSyncAt = lastSyncAt;
    }

    public void setLastSyncAt(Instant instant) {
        this.lastSyncAt = instant != null ? instant.toString() : null;
    }

    public String getLastResult() {
        return lastResult;
    }

    public void setLastResult(String lastResult) {
        this.lastResult = lastResult;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public Integer getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(Integer pendingCount) {
        this.pendingCount = pendingCount;
    }

    public Integer getProcessedCount() {
        return processedCount;
    }

    public void setProcessedCount(Integer processedCount) {
        this.processedCount = processedCount;
    }

    public Integer getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(Integer failedCount) {
        this.failedCount = failedCount;
    }

    public boolean isOauthConfigured() {
        return oauthConfigured;
    }

    public void setOauthConfigured(boolean oauthConfigured) {
        this.oauthConfigured = oauthConfigured;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getGoogleAccountEmail() {
        return googleAccountEmail;
    }

    public void setGoogleAccountEmail(String googleAccountEmail) {
        this.googleAccountEmail = googleAccountEmail;
    }
}
