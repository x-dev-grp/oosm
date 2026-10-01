package com.xdev.ooms.security.admin.dto;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardStatsDTO {
    private long totalTenants;
    private long activeTenants;
    private long inactiveTenants;
    private long totalUsers;
    private long activeUsers;
    private long lockedUsers;
    private long usersWithoutTenant;
    private long newUsersLast30Days;
    private List<AdminRoleCountDTO> usersByRole = new ArrayList<>();
    private List<AdminTenantSummaryDTO> topTenantsByUsers = new ArrayList<>();
    private List<AdminTenantSummaryDTO> recentTenants = new ArrayList<>();
    private List<AdminUserSummaryDTO> recentUsers = new ArrayList<>();
    private long activeUsersLast7Days;
    private List<AdminCompanyOverviewDTO> companies = new ArrayList<>();
    private List<AdminModuleAdoptionDTO> moduleAdoption = new ArrayList<>();
    private AdminSupportSummaryDTO supportTickets = new AdminSupportSummaryDTO();

    public long getActiveUsersLast7Days() {
        return activeUsersLast7Days;
    }

    public void setActiveUsersLast7Days(long activeUsersLast7Days) {
        this.activeUsersLast7Days = activeUsersLast7Days;
    }

    public List<AdminCompanyOverviewDTO> getCompanies() {
        return companies;
    }

    public void setCompanies(List<AdminCompanyOverviewDTO> companies) {
        this.companies = companies;
    }

    public List<AdminModuleAdoptionDTO> getModuleAdoption() {
        return moduleAdoption;
    }

    public void setModuleAdoption(List<AdminModuleAdoptionDTO> moduleAdoption) {
        this.moduleAdoption = moduleAdoption;
    }

    public AdminSupportSummaryDTO getSupportTickets() {
        return supportTickets;
    }

    public void setSupportTickets(AdminSupportSummaryDTO supportTickets) {
        this.supportTickets = supportTickets;
    }

    public long getTotalTenants() {
        return totalTenants;
    }

    public void setTotalTenants(long totalTenants) {
        this.totalTenants = totalTenants;
    }

    public long getActiveTenants() {
        return activeTenants;
    }

    public void setActiveTenants(long activeTenants) {
        this.activeTenants = activeTenants;
    }

    public long getInactiveTenants() {
        return inactiveTenants;
    }

    public void setInactiveTenants(long inactiveTenants) {
        this.inactiveTenants = inactiveTenants;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getLockedUsers() {
        return lockedUsers;
    }

    public void setLockedUsers(long lockedUsers) {
        this.lockedUsers = lockedUsers;
    }

    public long getUsersWithoutTenant() {
        return usersWithoutTenant;
    }

    public void setUsersWithoutTenant(long usersWithoutTenant) {
        this.usersWithoutTenant = usersWithoutTenant;
    }

    public long getNewUsersLast30Days() {
        return newUsersLast30Days;
    }

    public void setNewUsersLast30Days(long newUsersLast30Days) {
        this.newUsersLast30Days = newUsersLast30Days;
    }

    public List<AdminRoleCountDTO> getUsersByRole() {
        return usersByRole;
    }

    public void setUsersByRole(List<AdminRoleCountDTO> usersByRole) {
        this.usersByRole = usersByRole;
    }

    public List<AdminTenantSummaryDTO> getTopTenantsByUsers() {
        return topTenantsByUsers;
    }

    public void setTopTenantsByUsers(List<AdminTenantSummaryDTO> topTenantsByUsers) {
        this.topTenantsByUsers = topTenantsByUsers;
    }

    public List<AdminTenantSummaryDTO> getRecentTenants() {
        return recentTenants;
    }

    public void setRecentTenants(List<AdminTenantSummaryDTO> recentTenants) {
        this.recentTenants = recentTenants;
    }

    public List<AdminUserSummaryDTO> getRecentUsers() {
        return recentUsers;
    }

    public void setRecentUsers(List<AdminUserSummaryDTO> recentUsers) {
        this.recentUsers = recentUsers;
    }
}
