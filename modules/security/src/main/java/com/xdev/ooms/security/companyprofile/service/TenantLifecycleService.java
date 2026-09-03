package com.xdev.ooms.security.companyprofile.service;

import com.xdev.ooms.security.authorization.repository.AuthorizationRepository;
import com.xdev.ooms.security.companyprofile.dto.CompanyProfileDTO;
import com.xdev.ooms.security.companyprofile.entity.CompanyProfile;
import com.xdev.ooms.security.companyprofile.repository.CompanyProfileRepository;
import com.xdev.ooms.security.tenantmodule.entity.TenantEnabledModule;
import com.xdev.ooms.security.tenantmodule.repository.TenantEnabledModuleRepository;
import com.xdev.ooms.security.tenantmodule.service.TenantModuleService;
import com.xdev.ooms.security.user.entity.OOSMUser;
import com.xdev.ooms.security.user.repository.UserRepository;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import com.xdev.ooms.sharedkernel.utils.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TenantLifecycleService {

    private static final Set<String> PURGE_SKIP_TABLES = Set.of(
            "company_profile",
            "oosmuser",
            "tenant_enabled_module",
            "oauth2_registered_client",
            "oauth2_authorization",
            "oauth2_authorization_consent",
            "role",
            "permission",
            "roles_permissions",
            "app_setting",
            "app_setting_audit",
            "flyway_schema_history"
    );

    private final CompanyProfileRepository companyProfileRepository;
    private final UserRepository userRepository;
    private final TenantEnabledModuleRepository tenantEnabledModuleRepository;
    private final AuthorizationRepository authorizationRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ModelMapper modelMapper;
    private final TenantModuleService tenantModuleService;

    public TenantLifecycleService(
            CompanyProfileRepository companyProfileRepository,
            UserRepository userRepository,
            TenantEnabledModuleRepository tenantEnabledModuleRepository,
            AuthorizationRepository authorizationRepository,
            JdbcTemplate jdbcTemplate,
            ModelMapper modelMapper,
            TenantModuleService tenantModuleService
    ) {
        this.companyProfileRepository = companyProfileRepository;
        this.userRepository = userRepository;
        this.tenantEnabledModuleRepository = tenantEnabledModuleRepository;
        this.authorizationRepository = authorizationRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.modelMapper = modelMapper;
        this.tenantModuleService = tenantModuleService;
    }

    @Transactional
    public CompanyProfileDTO deactivate(UUID tenantId) {
        requireOosmAdmin();
        CompanyProfile company = requireCompany(tenantId);
        if (Boolean.TRUE.equals(company.getDeleted()) && !company.isActive()) {
            return toDto(company);
        }

        company.setActive(false);
        company.setDeleted(true);
        companyProfileRepository.save(company);

        int users = userRepository.deactivateUsersForTenant(tenantId);
        softDeleteModules(tenantId);

        OOSMLogger.logSecurityEvent(getClass(), "TENANT_DEACTIVATED",
                "Tenant deactivated: " + tenantId + " usersAffected=" + users);
        return toDto(company);
    }

    @Transactional
    public CompanyProfileDTO reactivate(UUID tenantId) {
        requireOosmAdmin();
        CompanyProfile company = requireCompany(tenantId);

        company.setActive(true);
        company.setDeleted(false);
        companyProfileRepository.save(company);

        int users = userRepository.reactivateUsersForTenant(tenantId);
        restoreModules(tenantId);

        OOSMLogger.logSecurityEvent(getClass(), "TENANT_REACTIVATED",
                "Tenant reactivated: " + tenantId + " usersAffected=" + users);
        return toDto(company);
    }

    @Transactional
    public void purge(UUID tenantId, String confirmationName) {
        requireOosmAdmin();
        CompanyProfile company = requireCompany(tenantId);

        String expected = company.getLegalName() == null ? "" : company.getLegalName().trim();
        String provided = confirmationName == null ? "" : confirmationName.trim();
        if (!StringUtils.hasText(expected) || !expected.equals(provided)) {
            throw new IllegalArgumentException("Confirmation name does not match company legal name");
        }

        List<OOSMUser> users = userRepository.findByTenantId(tenantId);
        List<String> usernames = users.stream()
                .map(OOSMUser::getUsername)
                .filter(StringUtils::hasText)
                .toList();

        int operationalDeleted = purgeTenantScopedTables(tenantId);

        if (!usernames.isEmpty()) {
            authorizationRepository.deleteByPrincipalNameIn(usernames);
        }

        int usersDeleted = userRepository.deleteByTenantId(tenantId);
        tenantEnabledModuleRepository.deleteByCompanyTenantId(tenantId);
        companyProfileRepository.delete(company);

        OOSMLogger.logSecurityEvent(getClass(), "TENANT_PURGED",
                "Tenant purged: " + tenantId
                        + " operationalRows≈" + operationalDeleted
                        + " usersDeleted=" + usersDeleted);
    }

    private int purgeTenantScopedTables(UUID tenantId) {
        List<String> tables = jdbcTemplate.queryForList("""
                SELECT c.table_name
                FROM information_schema.columns c
                JOIN information_schema.tables t
                  ON t.table_schema = c.table_schema AND t.table_name = c.table_name
                WHERE c.table_schema = 'public'
                  AND c.column_name = 'tenant_id'
                  AND t.table_type = 'BASE TABLE'
                ORDER BY c.table_name
                """, String.class);

        List<String> targets = tables.stream()
                .filter(name -> !PURGE_SKIP_TABLES.contains(name.toLowerCase(Locale.ROOT)))
                .collect(Collectors.toCollection(ArrayList::new));

        int totalDeleted = 0;
        Set<String> remaining = new HashSet<>(targets);
        final int maxRounds = Math.max(8, targets.size() + 2);

        for (int round = 0; round < maxRounds && !remaining.isEmpty(); round++) {
            Set<String> progressed = new HashSet<>();
            for (String table : List.copyOf(remaining)) {
                try {
                    Integer deleted = jdbcTemplate.update(
                            "DELETE FROM " + quoteIdent(table) + " WHERE tenant_id = ?",
                            tenantId);
                    totalDeleted += deleted == null ? 0 : deleted;
                    progressed.add(table);
                } catch (Exception ex) {
                    OOSMLogger.debug(getClass(),
                            "Tenant purge round {} deferred table {}: {}",
                            round, table, ex.getMessage());
                }
            }
            remaining.removeAll(progressed);
            if (progressed.isEmpty() && !remaining.isEmpty()) {
                throw new IllegalStateException(
                        "Unable to purge tenant data; FK blockers remain for tables: " + remaining);
            }
        }

        if (!remaining.isEmpty()) {
            throw new IllegalStateException(
                    "Unable to purge tenant data; leftover tables: " + remaining);
        }
        return totalDeleted;
    }

    private static String quoteIdent(String table) {
        if (!table.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Unsafe table name: " + table);
        }
        return table;
    }

    private void softDeleteModules(UUID tenantId) {
        List<TenantEnabledModule> rows = tenantEnabledModuleRepository.findByCompanyTenantId(tenantId);
        for (TenantEnabledModule row : rows) {
            if (!Boolean.TRUE.equals(row.getDeleted())) {
                row.setDeleted(true);
                tenantEnabledModuleRepository.save(row);
            }
        }
    }

    private void restoreModules(UUID tenantId) {
        List<TenantEnabledModule> rows = tenantEnabledModuleRepository.findByCompanyTenantId(tenantId);
        for (TenantEnabledModule row : rows) {
            if (Boolean.TRUE.equals(row.getDeleted())) {
                row.setDeleted(false);
                tenantEnabledModuleRepository.save(row);
            }
        }
    }

    private CompanyProfile requireCompany(UUID tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant id is required");
        }
        return companyProfileRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found: " + tenantId));
    }

    private void requireOosmAdmin() {
        if (!SecurityUtils.isOosmAdmin()) {
            throw new AccessDeniedException("Only OOSM administrators can manage company lifecycle");
        }
    }

    private CompanyProfileDTO toDto(CompanyProfile company) {
        CompanyProfileDTO dto = modelMapper.map(company, CompanyProfileDTO.class);
        dto.setActive(company.isActive());
        dto.setDeleted(Boolean.TRUE.equals(company.getDeleted()));
        if (dto.getId() != null) {
            dto.setEnabledModules(tenantModuleService.getEnabledModuleNames(dto.getId()));
        }
        return dto;
    }
}
