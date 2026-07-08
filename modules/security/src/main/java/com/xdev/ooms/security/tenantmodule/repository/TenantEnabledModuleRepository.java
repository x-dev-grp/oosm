package com.xdev.ooms.security.tenantmodule.repository;

import com.xdev.ooms.security.tenantmodule.entity.TenantEnabledModule;
import com.xdev.ooms.sharedkernel.models.OOSMModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantEnabledModuleRepository extends JpaRepository<TenantEnabledModule, UUID> {

    List<TenantEnabledModule> findByCompanyTenantId(UUID companyTenantId);

    List<TenantEnabledModule> findByCompanyTenantIdAndIsDeletedFalse(UUID companyTenantId);

    long countByCompanyTenantIdAndIsDeletedFalse(UUID companyTenantId);

    Optional<TenantEnabledModule> findByCompanyTenantIdAndModule(UUID companyTenantId, OOSMModule module);

    boolean existsByCompanyTenantIdAndModuleAndIsDeletedFalse(UUID companyTenantId, OOSMModule module);
}
