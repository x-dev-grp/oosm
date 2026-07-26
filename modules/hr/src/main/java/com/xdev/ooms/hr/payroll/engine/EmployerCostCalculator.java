package com.xdev.ooms.hr.payroll.engine;

import com.xdev.ooms.hr.common.HrMoney;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Employer cost = gross + employer CNSS (+ accident contribution when present).
 */
@Component
public class EmployerCostCalculator {

    public BigDecimal calculate(BigDecimal grossSalary, BigDecimal employerCnss, BigDecimal accidentContribution) {
        return HrMoney.money(
                HrMoney.money(grossSalary)
                        .add(HrMoney.money(employerCnss), HrMoney.MC)
                        .add(HrMoney.money(safe(accidentContribution)), HrMoney.MC));
    }

    private static BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
