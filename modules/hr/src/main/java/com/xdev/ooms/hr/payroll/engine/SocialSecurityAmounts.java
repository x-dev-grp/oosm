package com.xdev.ooms.hr.payroll.engine;

import java.math.BigDecimal;

public class SocialSecurityAmounts {

    private final BigDecimal employeeCnss;
    private final BigDecimal employerCnss;
    private final BigDecimal css;
    private final BigDecimal accidentContribution;

    public SocialSecurityAmounts(
            BigDecimal employeeCnss,
            BigDecimal employerCnss,
            BigDecimal css,
            BigDecimal accidentContribution
    ) {
        this.employeeCnss = employeeCnss;
        this.employerCnss = employerCnss;
        this.css = css;
        this.accidentContribution = accidentContribution;
    }

    public BigDecimal getEmployeeCnss() {
        return employeeCnss;
    }

    public BigDecimal getEmployerCnss() {
        return employerCnss;
    }

    public BigDecimal getCss() {
        return css;
    }

    public BigDecimal getAccidentContribution() {
        return accidentContribution;
    }
}
