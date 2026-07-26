package com.xdev.ooms.hr.payroll.engine;

import com.xdev.ooms.hr.common.HrMoney;
import com.xdev.ooms.hr.legal.entity.SocialSecurityConfiguration;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Employee / employer CNSS and CSS from {@link SocialSecurityConfiguration} rates (never hardcoded).
 */
@Component
public class SocialSecurityCalculator {

    public SocialSecurityAmounts calculate(BigDecimal cnssBase, SocialSecurityConfiguration config) {
        if (config == null) {
            throw new IllegalStateException(
                    "Social security configuration is missing. Seed legal rules via POST /api/hr/legal-rules/seed-defaults");
        }
        BigDecimal base = HrMoney.money(cnssBase != null ? cnssBase.max(BigDecimal.ZERO) : BigDecimal.ZERO);
        BigDecimal employeeRate = requireRate(config.getEmployeeRate(), "employeeRate");
        BigDecimal employerRate = requireRate(config.getEmployerRate(), "employerRate");
        BigDecimal cssRate = requireRate(config.getCssRate(), "cssRate");
        BigDecimal accidentRate = config.getAccidentContributionRate() != null
                ? HrMoney.rate(config.getAccidentContributionRate())
                : HrMoney.rate(BigDecimal.ZERO);

        BigDecimal employeeCnss = HrMoney.money(base.multiply(employeeRate, HrMoney.MC));
        BigDecimal employerCnss = HrMoney.money(base.multiply(employerRate, HrMoney.MC));
        BigDecimal css = HrMoney.money(base.multiply(cssRate, HrMoney.MC));
        BigDecimal accident = HrMoney.money(base.multiply(accidentRate, HrMoney.MC));
        return new SocialSecurityAmounts(employeeCnss, employerCnss, css, accident);
    }

    private static BigDecimal requireRate(BigDecimal rate, String field) {
        if (rate == null) {
            throw new IllegalStateException(
                    "Social security " + field + " is missing. Seed legal rules via POST /api/hr/legal-rules/seed-defaults");
        }
        return HrMoney.rate(rate);
    }
}
