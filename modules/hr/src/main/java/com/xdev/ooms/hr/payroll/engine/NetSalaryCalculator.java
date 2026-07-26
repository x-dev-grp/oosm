package com.xdev.ooms.hr.payroll.engine;

import com.xdev.ooms.hr.common.HrMoney;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Net = gross − employeeCnss − tax − css − advances − loans − other deductions.
 */
@Component
public class NetSalaryCalculator {

    public BigDecimal calculate(
            BigDecimal grossSalary,
            BigDecimal employeeCnss,
            BigDecimal incomeTax,
            BigDecimal css,
            BigDecimal advanceDeduction,
            BigDecimal loanDeduction,
            BigDecimal otherDeductions
    ) {
        BigDecimal net = HrMoney.money(grossSalary)
                .subtract(HrMoney.money(employeeCnss), HrMoney.MC)
                .subtract(HrMoney.money(incomeTax), HrMoney.MC)
                .subtract(HrMoney.money(css), HrMoney.MC)
                .subtract(HrMoney.money(safe(advanceDeduction)), HrMoney.MC)
                .subtract(HrMoney.money(safe(loanDeduction)), HrMoney.MC)
                .subtract(HrMoney.money(safe(otherDeductions)), HrMoney.MC);
        if (net.compareTo(BigDecimal.ZERO) < 0) {
            return HrMoney.zero();
        }
        return HrMoney.money(net);
    }

    public BigDecimal totalEmployeeDeductions(
            BigDecimal employeeCnss,
            BigDecimal incomeTax,
            BigDecimal css,
            BigDecimal advanceDeduction,
            BigDecimal loanDeduction,
            BigDecimal otherDeductions
    ) {
        return HrMoney.money(
                HrMoney.money(employeeCnss)
                        .add(HrMoney.money(incomeTax), HrMoney.MC)
                        .add(HrMoney.money(css), HrMoney.MC)
                        .add(HrMoney.money(safe(advanceDeduction)), HrMoney.MC)
                        .add(HrMoney.money(safe(loanDeduction)), HrMoney.MC)
                        .add(HrMoney.money(safe(otherDeductions)), HrMoney.MC));
    }

    private static BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
