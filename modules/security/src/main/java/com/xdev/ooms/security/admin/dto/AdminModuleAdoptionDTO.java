package com.xdev.ooms.security.admin.dto;

public class AdminModuleAdoptionDTO {
    private String module;
    private long tenantCount;

    public AdminModuleAdoptionDTO() {
    }

    public AdminModuleAdoptionDTO(String module, long tenantCount) {
        this.module = module;
        this.tenantCount = tenantCount;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public long getTenantCount() {
        return tenantCount;
    }

    public void setTenantCount(long tenantCount) {
        this.tenantCount = tenantCount;
    }
}
