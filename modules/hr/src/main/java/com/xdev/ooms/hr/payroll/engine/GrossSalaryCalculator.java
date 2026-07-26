package com.xdev.ooms.hr.payroll.engine;

import com.xdev.ooms.hr.common.HrMoney;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Gross = base + overtime + bonuses + other earnings.
 */
@Component
public class GrossSalaryCalculator {

    public BigDecimal calculate(BigDecimal baseSalary, PayrollInputs inputs) {
        BigDecimal base = HrMoney.money(baseSalary);
        BigDecimal overtime = HrMoney.money(safe(inputs != null ? inputs.getOvertimeAmount() : null));
        BigDecimal bonuses = HrMoney.money(safe(inputs != null ? inputs.getBonuses() : null));
        BigDecimal other = HrMoney.money(safe(inputs != null ? inputs.getOtherEarnings() : null));
        return HrMoney.money(base.add(overtime, HrMoney.MC).add(bonuses, HrMoney.MC).add(other, HrMoney.MC));
    }

    private static BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
