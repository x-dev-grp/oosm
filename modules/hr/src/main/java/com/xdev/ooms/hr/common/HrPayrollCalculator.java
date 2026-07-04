package com.xdev.ooms.hr.common;

/**
 * Tunisia payroll statutory calculations (CNSS 2025 rates, simplified IRPP/CSS).
 */
public final class HrPayrollCalculator {

    public static final double CNSS_EMPLOYEE_RATE = 0.0968;
    public static final double CNSS_EMPLOYER_RATE = 0.1707;
    public static final double CSS_RATE = 0.005;
    public static final double SMIG_MONTHLY_48H = 528.32;
    public static final double SMIG_MONTHLY_40H = 448.238;

    private HrPayrollCalculator() {
    }

    public static double cnssEmployee(double gross) {
        return round2(Math.max(0, gross) * CNSS_EMPLOYEE_RATE);
    }

    public static double cnssEmployer(double gross) {
        return round2(Math.max(0, gross) * CNSS_EMPLOYER_RATE);
    }

    public static double css(double gross) {
        return round2(Math.max(0, gross) * CSS_RATE);
    }

    /**
     * Progressive monthly IRPP on taxable income (gross minus employee CNSS).
     */
    public static double irpp(double taxableMonthly) {
        double taxable = Math.max(0, taxableMonthly);
        double tax = 0;
        tax += bracket(taxable, 0, 1500, 0);
        tax += bracket(taxable, 1500, 5000, 0.15);
        tax += bracket(taxable, 5000, 10000, 0.25);
        tax += bracket(taxable, 10000, 20000, 0.30);
        tax += bracket(taxable, 20000, Double.MAX_VALUE, 0.35);
        return round2(tax);
    }

    public static double netSalary(double gross, double cnssEmployee, double irpp, double css) {
        return round2(Math.max(0, gross) - cnssEmployee - irpp - css);
    }

    public static PayslipAmounts calculateFromGross(double gross) {
        double employeeCnss = cnssEmployee(gross);
        double employerCnss = cnssEmployer(gross);
        double cssAmount = css(gross);
        double irppAmount = irpp(gross - employeeCnss);
        double net = netSalary(gross, employeeCnss, irppAmount, cssAmount);
        return new PayslipAmounts(employeeCnss, employerCnss, irppAmount, cssAmount, net);
    }

    private static double bracket(double taxable, double from, double to, double rate) {
        if (taxable <= from) {
            return 0;
        }
        double upper = Math.min(taxable, to);
        return (upper - from) * rate;
    }

    public static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
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
