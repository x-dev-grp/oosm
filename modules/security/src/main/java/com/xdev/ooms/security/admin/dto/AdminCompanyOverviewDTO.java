package com.xdev.ooms.security.admin.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminCompanyOverviewDTO {
    private UUID tenantId;
    private String tenantName;
    private String city;
    private boolean active;
    private long userCount;
    private long activeUsersLast7Days;
    private List<String> enabledModules = new ArrayList<>();
    private LocalDateTime createdDate;
    private LocalDateTime lastActivityAt;

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getUserCount() {
        return userCount;
    }

    public void setUserCount(long userCount) {
        this.userCount = userCount;
    }

    public long getActiveUsersLast7Days() {
        return activeUsersLast7Days;
    }

    public void setActiveUsersLast7Days(long activeUsersLast7Days) {
        this.activeUsersLast7Days = activeUsersLast7Days;
    }

    public List<String> getEnabledModules() {
        return enabledModules;
    }

    public void setEnabledModules(List<String> enabledModules) {
        this.enabledModules = enabledModules;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }
}
