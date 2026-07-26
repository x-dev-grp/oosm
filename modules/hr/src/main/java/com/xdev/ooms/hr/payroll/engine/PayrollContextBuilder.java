package com.xdev.ooms.hr.payroll.engine;

import com.xdev.ooms.hr.common.HrMoney;
import com.xdev.ooms.hr.common.enums.WorkRegime;
import com.xdev.ooms.hr.contract.entity.EmploymentContract;
import com.xdev.ooms.hr.employee.entity.Employee;
import com.xdev.ooms.hr.legal.enums.WeeklyRegimeType;
import com.xdev.ooms.hr.payroll.entity.PayrollPeriod;
import com.xdev.ooms.hr.payslip.entity.Payslip;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Assembles payroll calculation inputs from employee / contract / period / payslip state.
 */
@Component
public class PayrollContextBuilder {

    public PayrollInputs fromPayslip(Payslip payslip) {
        PayrollInputs inputs = new PayrollInputs();
        if (payslip == null) {
            return inputs;
        }
        inputs.setBonuses(toMoney(payslip.getBonuses()));
        inputs.setOtherDeductions(toMoney(payslip.getOtherDeductions()));
        return inputs;
    }

    public BigDecimal resolveBaseSalary(Payslip payslip, EmploymentContract contract) {
        if (payslip != null && payslip.getBaseSalary() != null) {
            return HrMoney.money(BigDecimal.valueOf(payslip.getBaseSalary()));
        }
        if (contract != null) {
            if (contract.getBaseSalary() != null) {
                return HrMoney.money(contract.getBaseSalary());
            }
            if (contract.getSalary() != null) {
                return HrMoney.money(BigDecimal.valueOf(contract.getSalary()));
            }
        }
        return HrMoney.zero();
    }

    public LocalDate resolveAsOf(PayrollPeriod period) {
        if (period != null && period.getPeriodEnd() != null) {
            return period.getPeriodEnd();
        }
        if (period != null && period.getPeriodStart() != null) {
            return period.getPeriodStart();
        }
        return LocalDate.now();
    }

    public WeeklyRegimeType resolveWeeklyRegime(Employee employee, WeeklyRegimeType fallback) {
        if (employee != null && employee.getWorkRegime() != null) {
            return mapWorkRegime(employee.getWorkRegime());
        }
        return fallback != null ? fallback : WeeklyRegimeType.HOURS_48;
    }

    public WeeklyRegimeType mapWorkRegime(WorkRegime workRegime) {
        if (workRegime == null) {
            return WeeklyRegimeType.HOURS_48;
        }
        return switch (workRegime) {
            case HOURS_40 -> WeeklyRegimeType.HOURS_40;
            case HOURS_48 -> WeeklyRegimeType.HOURS_48;
            case AGRICULTURAL -> WeeklyRegimeType.HOURS_48;
        };
    }

    private static BigDecimal toMoney(Double value) {
        if (value == null) {
            return HrMoney.zero();
        }
        return HrMoney.money(BigDecimal.valueOf(value));
    }
}
