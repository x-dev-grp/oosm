package com.xdev.ooms.hr.payroll.engine;

import com.xdev.ooms.hr.common.HrMoney;
import com.xdev.ooms.hr.legal.entity.MinimumWageRule;
import com.xdev.ooms.hr.legal.enums.WeeklyRegimeType;
import com.xdev.ooms.hr.legal.service.LegalConfigurationService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates base salary against applicable minimum wage; adds anomaly codes to the result.
 */
@Component
public class PayrollValidator {

    public static final String SALARY_BELOW_MINIMUM = "SALARY_BELOW_MINIMUM";

    private final LegalConfigurationService legalConfigurationService;

    public PayrollValidator(LegalConfigurationService legalConfigurationService) {
        this.legalConfigurationService = legalConfigurationService;
    }

    /**
     * Pure validation when the minimum wage rule is already resolved (unit-test friendly).
     */
    public List<String> validateAgainstMinimum(BigDecimal baseSalary, MinimumWageRule minimumWageRule) {
        List<String> anomalies = new ArrayList<>();
        if (minimumWageRule == null || minimumWageRule.getMonthlyMinimum() == null) {
            return anomalies;
        }
        BigDecimal base = HrMoney.money(baseSalary);
        BigDecimal minimum = HrMoney.money(minimumWageRule.getMonthlyMinimum());
        if (base.compareTo(minimum) < 0) {
            anomalies.add(SALARY_BELOW_MINIMUM);
        }
        return anomalies;
    }

    public List<String> validate(
            BigDecimal baseSalary,
            WeeklyRegimeType workRegime,
            LocalDate asOf,
            String minimumWageProfile
    ) {
        String profile = minimumWageProfile != null && !minimumWageProfile.isBlank()
                ? minimumWageProfile
                : "SMIG";
        WeeklyRegimeType regime = workRegime != null ? workRegime : WeeklyRegimeType.HOURS_48;
        MinimumWageRule rule = legalConfigurationService.resolveMinimumWage(profile, regime, asOf);
        return validateAgainstMinimum(baseSalary, rule);
    }
}
