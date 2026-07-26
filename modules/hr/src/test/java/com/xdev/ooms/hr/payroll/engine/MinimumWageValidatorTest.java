package com.xdev.ooms.hr.payroll.engine;

import com.xdev.ooms.hr.common.HrMoney;
import com.xdev.ooms.hr.legal.entity.MinimumWageRule;
import com.xdev.ooms.hr.legal.enums.WeeklyRegimeType;
import com.xdev.ooms.hr.legal.service.LegalConfigurationSeedService;
import com.xdev.ooms.hr.legal.service.LegalConfigurationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class MinimumWageValidatorTest {

    private PayrollValidator validator;
    private MinimumWageRule rule;

    @BeforeEach
    void setUp() {
        validator = new PayrollValidator(mock(LegalConfigurationService.class));
        rule = new MinimumWageRule();
        rule.setWeeklyRegime(WeeklyRegimeType.HOURS_48);
        rule.setMonthlyMinimum(HrMoney.money(LegalConfigurationSeedService.PROVISIONAL_SMIG_48H));
    }

    @Test
    void noAnomalyWhenAtOrAboveMinimum() {
        List<String> anomalies = validator.validateAgainstMinimum(
                LegalConfigurationSeedService.PROVISIONAL_SMIG_48H,
                rule);
        assertTrue(anomalies.isEmpty());
    }

    @Test
    void flagsSalaryBelowMinimum() {
        List<String> anomalies = validator.validateAgainstMinimum(new BigDecimal("100"), rule);
        assertEquals(List.of(PayrollValidator.SALARY_BELOW_MINIMUM), anomalies);
    }
}
