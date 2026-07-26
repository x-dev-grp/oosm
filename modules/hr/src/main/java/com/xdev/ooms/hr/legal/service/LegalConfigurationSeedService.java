package com.xdev.ooms.hr.legal.service;

import com.xdev.ooms.hr.common.HrMoney;
import com.xdev.ooms.hr.legal.entity.CompanyHrLegalProfile;
import com.xdev.ooms.hr.legal.entity.LegalRule;
import com.xdev.ooms.hr.legal.entity.MinimumWageRule;
import com.xdev.ooms.hr.legal.entity.SalaryComponent;
import com.xdev.ooms.hr.legal.entity.SocialSecurityConfiguration;
import com.xdev.ooms.hr.legal.entity.TaxBracket;
import com.xdev.ooms.hr.legal.entity.TaxConfiguration;
import com.xdev.ooms.hr.legal.enums.BusinessActivityType;
import com.xdev.ooms.hr.legal.enums.CalculationType;
import com.xdev.ooms.hr.legal.enums.ComponentType;
import com.xdev.ooms.hr.legal.enums.LegalRuleCategory;
import com.xdev.ooms.hr.legal.enums.WeeklyRegimeType;
import com.xdev.ooms.hr.legal.repository.CompanyHrLegalProfileRepository;
import com.xdev.ooms.hr.legal.repository.LegalRuleRepository;
import com.xdev.ooms.hr.legal.repository.MinimumWageRuleRepository;
import com.xdev.ooms.hr.legal.repository.SalaryComponentRepository;
import com.xdev.ooms.hr.legal.repository.SocialSecurityConfigurationRepository;
import com.xdev.ooms.hr.legal.repository.TaxBracketRepository;
import com.xdev.ooms.hr.legal.repository.TaxConfigurationRepository;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.utils.AuditHelper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Seeds provisional Tunisian HR legal defaults for the current tenant.
 * Values are documented as provisional and must be validated against current law.
 */
@Component
public class LegalConfigurationSeedService {

    /** Provisional CNSS employee rate (Tunisia). Validate before production use. */
    public static final BigDecimal PROVISIONAL_CNSS_EMPLOYEE = new BigDecimal("0.0968");
    /** Provisional CNSS employer rate (Tunisia). Validate before production use. */
    public static final BigDecimal PROVISIONAL_CNSS_EMPLOYER = new BigDecimal("0.1707");
    /** Provisional CSS rate (Tunisia). Validate before production use. */
    public static final BigDecimal PROVISIONAL_CSS = new BigDecimal("0.005");
    /** Provisional SMIG monthly 40h. Validate before production use. */
    public static final BigDecimal PROVISIONAL_SMIG_40H = new BigDecimal("448.238");
    /** Provisional SMIG monthly 48h. Validate before production use. */
    public static final BigDecimal PROVISIONAL_SMIG_48H = new BigDecimal("528.32");

    private static final LocalDate DEFAULT_FROM = LocalDate.of(2024, 1, 1);
    private static final String PROVISIONAL_NOTE =
            "PROVISIONAL Tunisian defaults — validate against current legislation before production use.";

    private final SocialSecurityConfigurationRepository socialSecurityConfigurationRepository;
    private final TaxConfigurationRepository taxConfigurationRepository;
    private final TaxBracketRepository taxBracketRepository;
    private final MinimumWageRuleRepository minimumWageRuleRepository;
    private final SalaryComponentRepository salaryComponentRepository;
    private final LegalRuleRepository legalRuleRepository;
    private final CompanyHrLegalProfileRepository companyHrLegalProfileRepository;

    public LegalConfigurationSeedService(
            SocialSecurityConfigurationRepository socialSecurityConfigurationRepository,
            TaxConfigurationRepository taxConfigurationRepository,
            TaxBracketRepository taxBracketRepository,
            MinimumWageRuleRepository minimumWageRuleRepository,
            SalaryComponentRepository salaryComponentRepository,
            LegalRuleRepository legalRuleRepository,
            CompanyHrLegalProfileRepository companyHrLegalProfileRepository
    ) {
        this.socialSecurityConfigurationRepository = socialSecurityConfigurationRepository;
        this.taxConfigurationRepository = taxConfigurationRepository;
        this.taxBracketRepository = taxBracketRepository;
        this.minimumWageRuleRepository = minimumWageRuleRepository;
        this.salaryComponentRepository = salaryComponentRepository;
        this.legalRuleRepository = legalRuleRepository;
        this.companyHrLegalProfileRepository = companyHrLegalProfileRepository;
    }

    /**
     * Seeds default Tunisian legal rules for the current tenant if none exist yet.
     *
     * @return summary map (seeded=true/false and counts)
     */
    @Transactional
    public Map<String, Object> ensureDefaultTunisianRules() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("No current tenant in TenantContext");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("tenantId", tenantId.toString());
        result.put("provisional", true);
        result.put("note", PROVISIONAL_NOTE);

        if (socialSecurityConfigurationRepository.existsByTenantIdAndIsDeletedFalse(tenantId)) {
            result.put("seeded", false);
            result.put("reason", "SocialSecurityConfiguration already exists for tenant");
            return result;
        }

        seedSocialSecurity();
        TaxConfiguration taxConfig = seedTaxConfiguration();
        seedTaxBrackets(taxConfig);
        seedMinimumWages();
        seedSalaryComponents();
        seedLegalRules();
        seedCompanyProfileIfAbsent();

        result.put("seeded", true);
        result.put("socialSecurity", 1);
        result.put("taxConfiguration", 1);
        result.put("taxBrackets", 5);
        result.put("minimumWageRules", 2);
        result.put("salaryComponents", 10);
        return result;
    }

    private void seedSocialSecurity() {
        SocialSecurityConfiguration config = new SocialSecurityConfiguration();
        config.setRegime("CNSS_GENERAL");
        config.setEffectiveFrom(DEFAULT_FROM);
        config.setEmployeeRate(HrMoney.rate(PROVISIONAL_CNSS_EMPLOYEE));
        config.setEmployerRate(HrMoney.rate(PROVISIONAL_CNSS_EMPLOYER));
        config.setCssRate(HrMoney.rate(PROVISIONAL_CSS));
        config.setAccidentContributionRate(HrMoney.rate(BigDecimal.ZERO));
        config.setCalculationBaseRule("GROSS_SALARY");
        config.setLegalReference(PROVISIONAL_NOTE);
        config.setVersion(1);
        config.setActive(true);
        AuditHelper.applyAuditOnCreate(config);
        socialSecurityConfigurationRepository.save(config);
    }

    private TaxConfiguration seedTaxConfiguration() {
        TaxConfiguration tax = new TaxConfiguration();
        tax.setFiscalYear(DEFAULT_FROM.getYear());
        tax.setEffectiveFrom(DEFAULT_FROM);
        tax.setDescription("Provisional monthly IRPP brackets — " + PROVISIONAL_NOTE);
        tax.setVersion(1);
        tax.setActive(true);
        AuditHelper.applyAuditOnCreate(tax);
        return taxConfigurationRepository.save(tax);
    }

    private void seedTaxBrackets(TaxConfiguration taxConfig) {
        // Provisional IRPP monthly brackets: 0-1500 @0%, 1500-5000 @15%, 5000-10000 @25%, 10000-20000 @30%, 20000+ @35%
        saveBracket(taxConfig, "0", "1500", "0", 1);
        saveBracket(taxConfig, "1500", "5000", "0.15", 2);
        saveBracket(taxConfig, "5000", "10000", "0.25", 3);
        saveBracket(taxConfig, "10000", "20000", "0.30", 4);
        saveBracket(taxConfig, "20000", null, "0.35", 5);
    }

    private void saveBracket(
            TaxConfiguration taxConfig,
            String min,
            String max,
            String rate,
            int sortOrder
    ) {
        TaxBracket bracket = new TaxBracket();
        bracket.setTaxConfiguration(taxConfig);
        bracket.setMinAmount(HrMoney.money(new BigDecimal(min)));
        bracket.setMaxAmount(max != null ? HrMoney.money(new BigDecimal(max)) : null);
        bracket.setRate(HrMoney.rate(new BigDecimal(rate)));
        bracket.setSortOrder(sortOrder);
        AuditHelper.applyAuditOnCreate(bracket);
        taxBracketRepository.save(bracket);
    }

    private void seedMinimumWages() {
        saveSmig(WeeklyRegimeType.HOURS_40, PROVISIONAL_SMIG_40H);
        saveSmig(WeeklyRegimeType.HOURS_48, PROVISIONAL_SMIG_48H);
    }

    private void saveSmig(WeeklyRegimeType regime, BigDecimal monthly) {
        MinimumWageRule rule = new MinimumWageRule();
        rule.setProfile("SMIG");
        rule.setSector("GENERAL");
        rule.setWeeklyRegime(regime);
        rule.setMonthlyMinimum(HrMoney.money(monthly));
        // Approximate hourly from monthly / (regime hours * 52 / 12)
        BigDecimal weeklyHours = regime == WeeklyRegimeType.HOURS_40
                ? new BigDecimal("40")
                : new BigDecimal("48");
        BigDecimal monthlyHours = weeklyHours
                .multiply(new BigDecimal("52"), HrMoney.MC)
                .divide(new BigDecimal("12"), HrMoney.MC);
        rule.setHourlyMinimum(HrMoney.money(monthly.divide(monthlyHours, HrMoney.MC)));
        rule.setEffectiveFrom(DEFAULT_FROM);
        rule.setLegalReference(PROVISIONAL_NOTE);
        rule.setActive(true);
        AuditHelper.applyAuditOnCreate(rule);
        minimumWageRuleRepository.save(rule);
    }

    private void seedSalaryComponents() {
        saveComponent("BASIC_SALARY", "Salaire de base", ComponentType.EARNING, CalculationType.FIXED, true, true, 10);
        saveComponent("OVERTIME", "Heures supplémentaires", ComponentType.EARNING, CalculationType.QUANTITY_RATE, true, true, 20);
        saveComponent("BONUS", "Prime", ComponentType.EARNING, CalculationType.MANUAL, true, true, 30);
        saveComponent("CNSS_EMPLOYEE", "CNSS salarié", ComponentType.DEDUCTION, CalculationType.PERCENTAGE, false, false, 40);
        saveComponent("INCOME_TAX", "IRPP", ComponentType.DEDUCTION, CalculationType.FORMULA, false, false, 50);
        saveComponent("CSS", "CSS", ComponentType.DEDUCTION, CalculationType.PERCENTAGE, false, false, 60);
        saveComponent("ABSENCE_DEDUCTION", "Retenue absence", ComponentType.DEDUCTION, CalculationType.QUANTITY_RATE, false, false, 70);
        saveComponent("ADVANCE_DEDUCTION", "Avance sur salaire", ComponentType.DEDUCTION, CalculationType.MANUAL, false, false, 80);
        saveComponent("LOAN_DEDUCTION", "Retenue prêt", ComponentType.DEDUCTION, CalculationType.MANUAL, false, false, 90);
        saveComponent("CNSS_EMPLOYER", "CNSS employeur", ComponentType.EMPLOYER_CONTRIBUTION, CalculationType.PERCENTAGE, false, false, 100);
    }

    private void saveComponent(
            String code,
            String labelFr,
            ComponentType type,
            CalculationType calculationType,
            boolean taxable,
            boolean cnssApplicable,
            int sortOrder
    ) {
        SalaryComponent component = new SalaryComponent();
        component.setCode(code);
        component.setLabelFr(labelFr);
        component.setLabelAr(null);
        component.setType(type);
        component.setCalculationType(calculationType);
        component.setTaxable(taxable);
        component.setCnssApplicable(cnssApplicable);
        component.setEffectiveFrom(DEFAULT_FROM);
        component.setActive(true);
        component.setSortOrder(sortOrder);
        AuditHelper.applyAuditOnCreate(component);
        salaryComponentRepository.save(component);
    }

    private void seedLegalRules() {
        saveLegalRule("CNSS_EMPLOYEE_RATE", LegalRuleCategory.CNSS, PROVISIONAL_CNSS_EMPLOYEE);
        saveLegalRule("CNSS_EMPLOYER_RATE", LegalRuleCategory.CNSS, PROVISIONAL_CNSS_EMPLOYER);
        saveLegalRule("CSS_RATE", LegalRuleCategory.TAX, PROVISIONAL_CSS);
        saveLegalRule("SMIG_40H_MONTHLY", LegalRuleCategory.MINIMUM_WAGE, PROVISIONAL_SMIG_40H);
        saveLegalRule("SMIG_48H_MONTHLY", LegalRuleCategory.MINIMUM_WAGE, PROVISIONAL_SMIG_48H);
    }

    private void saveLegalRule(String code, LegalRuleCategory category, BigDecimal value) {
        LegalRule rule = new LegalRule();
        rule.setCode(code);
        rule.setCategory(category);
        rule.setEffectiveFrom(DEFAULT_FROM);
        rule.setValue(value);
        rule.setDescription(PROVISIONAL_NOTE);
        rule.setLegalReference(PROVISIONAL_NOTE);
        rule.setVersion(1);
        rule.setActive(true);
        AuditHelper.applyAuditOnCreate(rule);
        legalRuleRepository.save(rule);
    }

    private void seedCompanyProfileIfAbsent() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (companyHrLegalProfileRepository.findFirstByTenantIdAndIsDeletedFalseOrderByCreatedDateDesc(tenantId).isPresent()) {
            return;
        }
        CompanyHrLegalProfile profile = new CompanyHrLegalProfile();
        profile.setBusinessActivity(BusinessActivityType.INDUSTRIAL_PROCESSING);
        profile.setEmploymentSector("OLIVE_OIL");
        profile.setCnssRegime("GENERAL");
        profile.setWeeklyRegime(WeeklyRegimeType.HOURS_48);
        profile.setMinimumWageProfile("SMIG");
        profile.setFiscalRegime("IRPP");
        profile.setNotes(PROVISIONAL_NOTE);
        AuditHelper.applyAuditOnCreate(profile);
        companyHrLegalProfileRepository.save(profile);
    }
}
