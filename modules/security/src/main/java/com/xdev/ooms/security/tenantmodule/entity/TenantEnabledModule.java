package com.xdev.ooms.security.tenantmodule.entity;

import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import com.xdev.ooms.sharedkernel.models.OOSMModule;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(
        name = "tenant_enabled_module",
        uniqueConstraints = @UniqueConstraint(columnNames = {"company_tenant_id", "module"})
)
public class TenantEnabledModule extends BaseEntity implements Serializable {

    @Column(name = "company_tenant_id", nullable = false)
    private UUID companyTenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OOSMModule module;

    public UUID getCompanyTenantId() {
        return companyTenantId;
    }

    public void setCompanyTenantId(UUID companyTenantId) {
        this.companyTenantId = companyTenantId;
    }

    public OOSMModule getModule() {
        return module;
    }

    public void setModule(OOSMModule module) {
        this.module = module;
    }
}
