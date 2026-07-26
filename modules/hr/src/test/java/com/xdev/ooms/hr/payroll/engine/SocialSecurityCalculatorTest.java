package com.xdev.ooms.hr.payroll.engine;

import com.xdev.ooms.hr.common.HrMoney;
import com.xdev.ooms.hr.legal.entity.SocialSecurityConfiguration;
import com.xdev.ooms.hr.legal.service.LegalConfigurationSeedService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SocialSecurityCalculatorTest {

    private SocialSecurityCalculator calculator;
    private SocialSecurityConfiguration config;

    @BeforeEach
    void setUp() {
        calculator = new SocialSecurityCalculator();
        config = new SocialSecurityConfiguration();
        config.setEmployeeRate(HrMoney.rate(LegalConfigurationSeedService.PROVISIONAL_CNSS_EMPLOYEE));
        config.setEmployerRate(HrMoney.rate(LegalConfigurationSeedService.PROVISIONAL_CNSS_EMPLOYER));
        config.setCssRate(HrMoney.rate(LegalConfigurationSeedService.PROVISIONAL_CSS));
        config.setAccidentContributionRate(HrMoney.rate(BigDecimal.ZERO));
    }

    @Test
    void calculatesEmployeeEmployerAndCssFromConfigRates() {
        SocialSecurityAmounts amounts = calculator.calculate(new BigDecimal("2000"), config);

        assertEquals(HrMoney.money(new BigDecimal("193.600")), amounts.getEmployeeCnss());
        assertEquals(HrMoney.money(new BigDecimal("341.400")), amounts.getEmployerCnss());
        assertEquals(HrMoney.money(new BigDecimal("10.000")), amounts.getCss());
        assertEquals(HrMoney.zero(), amounts.getAccidentContribution());
    }

    @Test
    void includesAccidentContributionWhenRatePresent() {
        config.setAccidentContributionRate(HrMoney.rate(new BigDecimal("0.01")));
        SocialSecurityAmounts amounts = calculator.calculate(new BigDecimal("1000"), config);
        assertEquals(HrMoney.money(new BigDecimal("10.000")), amounts.getAccidentContribution());
    }

    @Test
    void throwsWhenConfigMissing() {
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> calculator.calculate(new BigDecimal("1000"), null));
        assertEquals(true, ex.getMessage().contains("seed-defaults"));
    }
}
