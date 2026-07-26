package com.xdev.ooms.hr.common;

import com.xdev.ooms.hr.legal.entity.TaxConfiguration;
import com.xdev.ooms.hr.legal.enums.WeeklyRegimeType;
import com.xdev.ooms.hr.legal.service.LegalConfigurationService;
import com.xdev.ooms.hr.payroll.engine.IncomeTaxCalculator;
import com.xdev.ooms.hr.payroll.engine.PayrollEngine;
import com.xdev.ooms.hr.payroll.engine.PayrollInputs;
import com.xdev.ooms.hr.payroll.engine.PayrollResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Facade over the deterministic {@link PayrollEngine}.
 * Rates are never hardcoded — they come from LegalConfigurationService via the engine.
 */
@Component
public class HrPayrollCalculator {

    private final PayrollEngine payrollEngine;
    private final LegalConfigurationService legalConfigurationService;
    private final IncomeTaxCalculator incomeTaxCalculator;

    public HrPayrollCalculator(
            PayrollEngine payrollEngine,
            LegalConfigurationService legalConfigurationService,
            IncomeTaxCalculator incomeTaxCalculator
    ) {
        this.payrollEngine = payrollEngine;
        this.legalConfigurationService = legalConfigurationService;
        this.incomeTaxCalculator = incomeTaxCalculator;
    }

    public double cnssEmployee(double gross, LocalDate asOf) {
        return calculateFromGross(gross, asOf, WeeklyRegimeType.HOURS_48).cnssEmployee();
    }

    public double cnssEmployer(double gross, LocalDate asOf) {
        return calculateFromGross(gross, asOf, WeeklyRegimeType.HOURS_48).cnssEmployer();
    }

    public double css(double gross, LocalDate asOf) {
        return calculateFromGross(gross, asOf, WeeklyRegimeType.HOURS_48).css();
    }

    public double irpp(double taxableMonthly, LocalDate asOf) {
        LocalDate date = asOf != null ? asOf : LocalDate.now();
        TaxConfiguration taxConfig = legalConfigurationService.resolveTaxConfiguration(date);
        if (taxConfig == null) {
            throw new IllegalStateException(
                    "Tax configuration is missing. Seed legal rules via POST /api/hr/legal-rules/seed-defaults");
        }
        return toDouble(incomeTaxCalculator.calculate(BigDecimal.valueOf(taxableMonthly), taxConfig));
    }

    public double netSalary(double gross, double cnssEmployee, double irpp, double css) {
        return round2(Math.max(0, gross) - cnssEmployee - irpp - css);
    }

    public PayslipAmounts calculateFromGross(double gross, LocalDate asOf, WeeklyRegimeType regime) {
        PayrollResult result = payrollEngine.calculate(
                BigDecimal.valueOf(gross),
                new PayrollInputs(),
                asOf != null ? asOf : LocalDate.now(),
                regime != null ? regime : WeeklyRegimeType.HOURS_48
        );
        return new PayslipAmounts(
                toDouble(result.getEmployeeCnss()),
                toDouble(result.getEmployerCnss()),
                toDouble(result.getIncomeTax()),
                toDouble(result.getCss()),
                toDouble(result.getNetSalary())
        );
    }

    public PayrollResult calculate(
            BigDecimal baseSalary,
            PayrollInputs inputs,
            LocalDate asOf,
            WeeklyRegimeType regime
    ) {
        return payrollEngine.calculate(baseSalary, inputs, asOf, regime);
    }

    public static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static double toDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : 0d;
    }

    public record PayslipAmounts(
            double cnssEmployee,
            double cnssEmployer,
            double irpp,
            double css,
            double netSalary
    ) {
    }
}
