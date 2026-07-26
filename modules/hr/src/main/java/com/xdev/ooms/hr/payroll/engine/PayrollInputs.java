package com.xdev.ooms.hr.payroll.engine;

import java.math.BigDecimal;

/**
 * Variable earnings / deductions for a payroll calculation run.
 */
public class PayrollInputs {

    private BigDecimal bonuses = BigDecimal.ZERO;
    private BigDecimal overtimeAmount = BigDecimal.ZERO;
    private BigDecimal absenceDeduction = BigDecimal.ZERO;
    private BigDecimal advanceDeduction = BigDecimal.ZERO;
    private BigDecimal loanDeduction = BigDecimal.ZERO;
    private BigDecimal otherEarnings = BigDecimal.ZERO;
    private BigDecimal otherDeductions = BigDecimal.ZERO;
    private BigDecimal workedDays;
    private BigDecimal overtimeHours;

    public BigDecimal getBonuses() {
        return bonuses;
    }

    public void setBonuses(BigDecimal bonuses) {
        this.bonuses = bonuses;
    }

    public BigDecimal getOvertimeAmount() {
        return overtimeAmount;
    }

    public void setOvertimeAmount(BigDecimal overtimeAmount) {
        this.overtimeAmount = overtimeAmount;
    }

    public BigDecimal getAbsenceDeduction() {
        return absenceDeduction;
    }

    public void setAbsenceDeduction(BigDecimal absenceDeduction) {
        this.absenceDeduction = absenceDeduction;
    }

    public BigDecimal getAdvanceDeduction() {
        return advanceDeduction;
    }

    public void setAdvanceDeduction(BigDecimal advanceDeduction) {
        this.advanceDeduction = advanceDeduction;
    }

    public BigDecimal getLoanDeduction() {
        return loanDeduction;
    }

    public void setLoanDeduction(BigDecimal loanDeduction) {
        this.loanDeduction = loanDeduction;
    }

    public BigDecimal getOtherEarnings() {
        return otherEarnings;
    }

    public void setOtherEarnings(BigDecimal otherEarnings) {
        this.otherEarnings = otherEarnings;
    }

    public BigDecimal getOtherDeductions() {
        return otherDeductions;
    }

    public void setOtherDeductions(BigDecimal otherDeductions) {
        this.otherDeductions = otherDeductions;
    }

    public BigDecimal getWorkedDays() {
        return workedDays;
    }

    public void setWorkedDays(BigDecimal workedDays) {
        this.workedDays = workedDays;
    }

    public BigDecimal getOvertimeHours() {
        return overtimeHours;
    }

    public void setOvertimeHours(BigDecimal overtimeHours) {
        this.overtimeHours = overtimeHours;
    }
}
