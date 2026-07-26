package com.xdev.ooms.hr.legal.validation;

import com.xdev.ooms.hr.common.HrMoney;
import com.xdev.ooms.hr.legal.entity.MinimumWageRule;
import com.xdev.ooms.hr.legal.enums.WeeklyRegimeType;
import com.xdev.ooms.hr.legal.service.LegalConfigurationService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Validates a base salary against the effective Tunisian minimum wage for a weekly regime.
 */
@Component
public class SalaryComplianceValidator {

    private final LegalConfigurationService legalConfigurationService;

    public SalaryComplianceValidator(LegalConfigurationService legalConfigurationService) {
        this.legalConfigurationService = legalConfigurationService;
    }

    public ComplianceResult validate(BigDecimal baseSalary, WeeklyRegimeType regime, LocalDate asOf) {
        WeeklyRegimeType effectiveRegime = regime != null ? regime : WeeklyRegimeType.HOURS_48;
        LocalDate date = asOf != null ? asOf : LocalDate.now();
        MinimumWageRule rule = legalConfigurationService.resolveMinimumWage("SMIG", effectiveRegime, date);
        return validateAgainstRule(baseSalary, rule);
    }

    /**
     * Pure validation when the rule is already resolved (unit-test friendly).
     */
    public ComplianceResult validateAgainstRule(BigDecimal baseSalary, MinimumWageRule rule) {
        if (rule == null || rule.getMonthlyMinimum() == null) {
            throw new IllegalStateException(
                    "Minimum wage rule is missing. Seed legal rules via POST /api/hr/legal-rules/seed-defaults");
        }
        BigDecimal base = HrMoney.money(baseSalary);
        BigDecimal minimum = HrMoney.money(rule.getMonthlyMinimum());
        if (base.compareTo(minimum) < 0) {
            return ComplianceResult.nonCompliant(
                    minimum,
                    "Base salary " + base + " is below applicable minimum " + minimum);
        }
        return ComplianceResult.compliant(minimum);
    }
}
