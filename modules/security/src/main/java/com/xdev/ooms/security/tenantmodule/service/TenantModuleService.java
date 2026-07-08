package com.xdev.ooms.security.tenantmodule.service;

import com.xdev.ooms.security.tenantmodule.entity.TenantEnabledModule;
import com.xdev.ooms.security.tenantmodule.repository.TenantEnabledModuleRepository;
import com.xdev.ooms.sharedkernel.models.OOSMModule;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import com.xdev.ooms.sharedkernel.utils.SecurityUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TenantModuleService {

    private final TenantEnabledModuleRepository repository;

    public TenantModuleService(TenantEnabledModuleRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Set<OOSMModule> getEnabledModules(UUID tenantId) {
        if (tenantId == null) {
            return EnumSet.allOf(OOSMModule.class);
        }
        return repository.findByCompanyTenantIdAndIsDeletedFalse(tenantId).stream()
                .map(TenantEnabledModule::getModule)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(OOSMModule.class)));
    }

    @Transactional(readOnly = true)
    public List<String> getEnabledModuleNames(UUID tenantId) {
        return getEnabledModules(tenantId).stream()
                .map(Enum::name)
                .sorted()
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isModuleEnabled(UUID tenantId, OOSMModule module) {
        if (tenantId == null || module == null) {
            return true;
        }
        return repository.existsByCompanyTenantIdAndModuleAndIsDeletedFalse(tenantId, module);
    }

    @Transactional(readOnly = true)
    public boolean isAnyModuleEnabled(UUID tenantId, Set<OOSMModule> requiredModules) {
        if (tenantId == null || requiredModules == null || requiredModules.isEmpty()) {
            return true;
        }
        Set<OOSMModule> enabled = getEnabledModules(tenantId);
        for (OOSMModule module : requiredModules) {
            if (enabled.contains(module)) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    public Set<OOSMModule> setEnabledModules(UUID tenantId, Collection<String> moduleNames) {
        if (!SecurityUtils.isOosmAdmin()) {
            throw new AccessDeniedException("Only OOSM administrators can manage tenant modules");
        }
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant id is required");
        }
        Set<OOSMModule> modules = parseModules(moduleNames);
        if (modules.isEmpty()) {
            throw new IllegalArgumentException("At least one module must be enabled");
        }

        persistEnabledModules(tenantId, modules);

        OOSMLogger.info(getClass(), "Tenant {} enabled modules updated: {}", tenantId, modules);
        return modules;
    }

    @Transactional
    public void bootstrapAllModulesForTenant(UUID tenantId) {
        if (tenantId == null) {
            return;
        }
        if (repository.countByCompanyTenantIdAndIsDeletedFalse(tenantId) > 0) {
            return;
        }
        setEnabledModulesInternal(tenantId, EnumSet.allOf(OOSMModule.class));
        OOSMLogger.info(getClass(), "Bootstrapped all modules for existing tenant {}", tenantId);
    }

    public List<GrantedAuthority> filterAuthorities(UUID tenantId, Collection<? extends GrantedAuthority> authorities) {
        if (tenantId == null || authorities == null || authorities.isEmpty()) {
            return new ArrayList<>(authorities == null ? List.of() : authorities);
        }

        Set<OOSMModule> enabled = getEnabledModules(tenantId);
        if (enabled.isEmpty()) {
            List<GrantedAuthority> roleOnly = new ArrayList<>();
            for (GrantedAuthority authority : authorities) {
                if (!authority.getAuthority().contains(":")) {
                    roleOnly.add(authority);
                }
            }
            return roleOnly;
        }

        List<GrantedAuthority> filtered = new ArrayList<>();
        for (GrantedAuthority authority : authorities) {
            String value = authority.getAuthority();
            if (!StringUtils.hasText(value) || !value.contains(":")) {
                filtered.add(authority);
                continue;
            }
            String moduleToken = value.split(":")[0].trim().toUpperCase(Locale.ROOT);
            try {
                OOSMModule module = OOSMModule.valueOf(moduleToken);
                if (enabled.contains(module)) {
                    filtered.add(authority);
                }
            } catch (IllegalArgumentException ex) {
                filtered.add(authority);
            }
        }
        return filtered;
    }

    public Set<OOSMModule> parseModules(Collection<String> moduleNames) {
        if (moduleNames == null || moduleNames.isEmpty()) {
            return Set.of();
        }
        Set<OOSMModule> modules = new LinkedHashSet<>();
        for (String name : moduleNames) {
            if (!StringUtils.hasText(name)) {
                continue;
            }
            try {
                modules.add(OOSMModule.valueOf(name.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Unknown module: " + name);
            }
        }
        return modules;
    }

    private void setEnabledModulesInternal(UUID tenantId, Set<OOSMModule> modules) {
        persistEnabledModules(tenantId, modules);
    }

    private void persistEnabledModules(UUID tenantId, Set<OOSMModule> modules) {
        List<TenantEnabledModule> existing = repository.findByCompanyTenantId(tenantId);
        Map<OOSMModule, TenantEnabledModule> byModule = existing.stream()
                .collect(Collectors.toMap(TenantEnabledModule::getModule, Function.identity(), (left, right) -> left));

        for (OOSMModule module : modules) {
            TenantEnabledModule row = byModule.get(module);
            if (row == null) {
                row = new TenantEnabledModule();
                row.setCompanyTenantId(tenantId);
                row.setTenantId(tenantId);
                row.setModule(module);
            }
            row.setDeleted(false);
            repository.save(row);
        }

        for (TenantEnabledModule row : existing) {
            if (!modules.contains(row.getModule()) && !Boolean.TRUE.equals(row.getDeleted())) {
                row.setDeleted(true);
                repository.save(row);
            }
        }
    }
}
