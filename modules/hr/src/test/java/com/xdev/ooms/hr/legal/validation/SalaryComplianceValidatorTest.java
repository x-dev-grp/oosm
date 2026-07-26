package com.xdev.ooms.hr.legal.validation;

import com.xdev.ooms.hr.common.HrMoney;
import com.xdev.ooms.hr.legal.entity.MinimumWageRule;
import com.xdev.ooms.hr.legal.enums.WeeklyRegimeType;
import com.xdev.ooms.hr.legal.service.LegalConfigurationSeedService;
import com.xdev.ooms.hr.legal.service.LegalConfigurationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SalaryComplianceValidatorTest {

    private LegalConfigurationService legalConfigurationService;
    private SalaryComplianceValidator validator;
    private MinimumWageRule smig48;

    @BeforeEach
    void setUp() {
        legalConfigurationService = mock(LegalConfigurationService.class);
        validator = new SalaryComplianceValidator(legalConfigurationService);

        smig48 = new MinimumWageRule();
        smig48.setProfile("SMIG");
        smig48.setWeeklyRegime(WeeklyRegimeType.HOURS_48);
        smig48.setMonthlyMinimum(HrMoney.money(LegalConfigurationSeedService.PROVISIONAL_SMIG_48H));
    }

    @Test
    void compliantWhenAboveMinimum() {
        when(legalConfigurationService.resolveMinimumWage(eq("SMIG"), eq(WeeklyRegimeType.HOURS_48), any()))
                .thenReturn(smig48);

        ComplianceResult result = validator.validate(
                new BigDecimal("800"),
                WeeklyRegimeType.HOURS_48,
                LocalDate.of(2025, 1, 31));

        assertTrue(result.isCompliant());
        assertEquals(ComplianceStatus.COMPLIANT, result.getStatus());
        assertEquals(HrMoney.money(LegalConfigurationSeedService.PROVISIONAL_SMIG_48H), result.getApplicableMinimum());
    }

    @Test
    void nonCompliantWhenBelowMinimum() {
        ComplianceResult result = validator.validateAgainstRule(new BigDecimal("400"), smig48);

        assertFalse(result.isCompliant());
        assertEquals(ComplianceStatus.NON_COMPLIANT, result.getStatus());
        assertEquals(HrMoney.money(LegalConfigurationSeedService.PROVISIONAL_SMIG_48H), result.getApplicableMinimum());
    }

    @Test
    void throwsWhenRuleMissing() {
        assertThrows(IllegalStateException.class, () -> validator.validateAgainstRule(BigDecimal.TEN, null));
    }
}
