package com.xdev.ooms.hr.legal.service;

import com.xdev.ooms.hr.legal.dto.PayrollLegalSnapshot;
import com.xdev.ooms.hr.legal.entity.CompanyHrLegalProfile;
import com.xdev.ooms.hr.legal.entity.LegalRule;
import com.xdev.ooms.hr.legal.entity.MinimumWageRule;
import com.xdev.ooms.hr.legal.entity.SocialSecurityConfiguration;
import com.xdev.ooms.hr.legal.entity.TaxBracket;
import com.xdev.ooms.hr.legal.entity.TaxConfiguration;
import com.xdev.ooms.hr.legal.enums.LegalRuleCategory;
import com.xdev.ooms.hr.legal.enums.WeeklyRegimeType;
import com.xdev.ooms.hr.legal.repository.CompanyHrLegalProfileRepository;
import com.xdev.ooms.hr.legal.repository.LegalRuleRepository;
import com.xdev.ooms.hr.legal.repository.MinimumWageRuleRepository;
import com.xdev.ooms.hr.legal.repository.SocialSecurityConfigurationRepository;
import com.xdev.ooms.hr.legal.repository.TaxBracketRepository;
import com.xdev.ooms.hr.legal.repository.TaxConfigurationRepository;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Resolves effective Tunisian legal / payroll configuration for the current tenant.
 */
@Service
public class LegalConfigurationService {

    private final SocialSecurityConfigurationRepository socialSecurityConfigurationRepository;
    private final TaxConfigurationRepository taxConfigurationRepository;
    private final TaxBracketRepository taxBracketRepository;
    private final MinimumWageRuleRepository minimumWageRuleRepository;
    private final LegalRuleRepository legalRuleRepository;
    private final CompanyHrLegalProfileRepository companyHrLegalProfileRepository;

    public LegalConfigurationService(
            SocialSecurityConfigurationRepository socialSecurityConfigurationRepository,
            TaxConfigurationRepository taxConfigurationRepository,
            TaxBracketRepository taxBracketRepository,
            MinimumWageRuleRepository minimumWageRuleRepository,
            LegalRuleRepository legalRuleRepository,
            CompanyHrLegalProfileRepository companyHrLegalProfileRepository
    ) {
        this.socialSecurityConfigurationRepository = socialSecurityConfigurationRepository;
        this.taxConfigurationRepository = taxConfigurationRepository;
        this.taxBracketRepository = taxBracketRepository;
        this.minimumWageRuleRepository = minimumWageRuleRepository;
        this.legalRuleRepository = legalRuleRepository;
        this.companyHrLegalProfileRepository = companyHrLegalProfileRepository;
    }

    @Transactional(readOnly = true)
    public SocialSecurityConfiguration resolveSocialSecurity(LocalDate asOf) {
        UUID tenantId = requireTenant();
        LocalDate date = asOf != null ? asOf : LocalDate.now();
        List<SocialSecurityConfiguration> matches =
                socialSecurityConfigurationRepository.findEffective(tenantId, date);
        return matches.isEmpty() ? null : matches.get(0);
    }

    @Transactional(readOnly = true)
    public TaxConfiguration resolveTaxConfiguration(LocalDate asOf) {
        UUID tenantId = requireTenant();
        LocalDate date = asOf != null ? asOf : LocalDate.now();
        List<TaxConfiguration> matches = taxConfigurationRepository.findEffective(tenantId, date);
        if (matches.isEmpty()) {
            return null;
        }
        TaxConfiguration config = matches.get(0);
        List<TaxBracket> brackets =
                taxBracketRepository.findByTaxConfiguration_IdAndIsDeletedFalseOrderBySortOrderAsc(config.getId());
        config.setBrackets(brackets);
        return config;
    }

    @Transactional(readOnly = true)
    public MinimumWageRule resolveMinimumWage(String profile, WeeklyRegimeType regime, LocalDate asOf) {
        UUID tenantId = requireTenant();
        LocalDate date = asOf != null ? asOf : LocalDate.now();
        List<MinimumWageRule> matches =
                minimumWageRuleRepository.findEffective(tenantId, profile, regime, date);
        return matches.isEmpty() ? null : matches.get(0);
    }

    @Transactional(readOnly = true)
    public LegalRule resolveLegalRule(String code, LegalRuleCategory category, LocalDate asOf) {
        UUID tenantId = requireTenant();
        LocalDate date = asOf != null ? asOf : LocalDate.now();
        List<LegalRule> matches =
                legalRuleRepository.findEffectiveByCodeAndCategory(tenantId, code, category, date);
        return matches.isEmpty() ? null : matches.get(0);
    }

    @Transactional(readOnly = true)
    public PayrollLegalSnapshot buildPayrollLegalSnapshot(LocalDate asOf) {
        LocalDate date = asOf != null ? asOf : LocalDate.now();
        PayrollLegalSnapshot snapshot = new PayrollLegalSnapshot();
        snapshot.setAsOf(date);

        SocialSecurityConfiguration social = resolveSocialSecurity(date);
        if (social != null) {
            snapshot.setSocialSecurityConfigId(social.getId());
            snapshot.setSocialSecurityVersion(social.getVersion());
        }

        TaxConfiguration tax = resolveTaxConfiguration(date);
        if (tax != null) {
            snapshot.setTaxConfigurationId(tax.getId());
            snapshot.setTaxConfigurationVersion(tax.getVersion());
        }

        CompanyHrLegalProfile profile =
                companyHrLegalProfileRepository
                        .findFirstByTenantIdAndIsDeletedFalseOrderByCreatedDateDesc(requireTenant())
                        .orElse(null);
        if (profile != null) {
            snapshot.setCompanyLegalProfileId(profile.getId());
            snapshot.setMinimumWageProfile(profile.getMinimumWageProfile());
            WeeklyRegimeType regime = profile.getWeeklyRegime() != null
                    ? profile.getWeeklyRegime()
                    : WeeklyRegimeType.HOURS_48;
            String wageProfile = profile.getMinimumWageProfile() != null
                    ? profile.getMinimumWageProfile()
                    : "SMIG";
            MinimumWageRule wage = resolveMinimumWage(wageProfile, regime, date);
            if (wage != null) {
                snapshot.setMinimumWageRuleId(wage.getId());
            }
        } else {
            MinimumWageRule wage = resolveMinimumWage("SMIG", WeeklyRegimeType.HOURS_48, date);
            if (wage != null) {
                snapshot.setMinimumWageRuleId(wage.getId());
                snapshot.setMinimumWageProfile("SMIG");
            }
        }

        return snapshot;
    }

    private UUID requireTenant() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("No current tenant in TenantContext");
        }
        return tenantId;
    }
}
