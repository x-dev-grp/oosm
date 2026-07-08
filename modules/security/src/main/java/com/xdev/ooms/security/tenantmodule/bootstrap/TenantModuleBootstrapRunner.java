package com.xdev.ooms.security.tenantmodule.bootstrap;

import com.xdev.ooms.security.companyprofile.entity.CompanyProfile;
import com.xdev.ooms.security.companyprofile.repository.CompanyProfileRepository;
import com.xdev.ooms.security.tenantmodule.service.TenantModuleService;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TenantModuleBootstrapRunner implements ApplicationRunner {

    private final CompanyProfileRepository companyProfileRepository;
    private final TenantModuleService tenantModuleService;

    public TenantModuleBootstrapRunner(CompanyProfileRepository companyProfileRepository,
                                       TenantModuleService tenantModuleService) {
        this.companyProfileRepository = companyProfileRepository;
        this.tenantModuleService = tenantModuleService;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<CompanyProfile> companies = companyProfileRepository.findAll().stream()
                .filter(company -> !Boolean.TRUE.equals(company.getDeleted()))
                .toList();
        int bootstrapped = 0;
        for (CompanyProfile company : companies) {
            long before = tenantModuleService.getEnabledModules(company.getId()).size();
            tenantModuleService.bootstrapAllModulesForTenant(company.getId());
            if (before == 0 && !tenantModuleService.getEnabledModules(company.getId()).isEmpty()) {
                bootstrapped++;
            }
        }
        if (bootstrapped > 0) {
            OOSMLogger.info(getClass(), "Tenant module bootstrap: enabled all modules for {} existing tenant(s)", bootstrapped);
        }
    }
}
