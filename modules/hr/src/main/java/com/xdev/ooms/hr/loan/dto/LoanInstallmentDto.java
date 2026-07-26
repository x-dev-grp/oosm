package com.xdev.ooms.hr.loan.dto;

import com.xdev.ooms.hr.common.enums.LoanInstallmentStatus;
import com.xdev.ooms.hr.loan.entity.LoanInstallment;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LoanInstallmentDto extends BaseDto<LoanInstallment> {
    private EmployeeLoanDto loan;
    private LocalDate dueDate;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private Integer payrollPeriodYear;
    private Integer payrollPeriodMonth;
    private LoanInstallmentStatus status;

    public EmployeeLoanDto getLoan() {
        return loan;
    }

    public void setLoan(EmployeeLoanDto loan) {
        this.loan = loan;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public Integer getPayrollPeriodYear() {
        return payrollPeriodYear;
    }

    public void setPayrollPeriodYear(Integer payrollPeriodYear) {
        this.payrollPeriodYear = payrollPeriodYear;
    }

    public Integer getPayrollPeriodMonth() {
        return payrollPeriodMonth;
    }

    public void setPayrollPeriodMonth(Integer payrollPeriodMonth) {
        this.payrollPeriodMonth = payrollPeriodMonth;
    }

    public LoanInstallmentStatus getStatus() {
        return status;
    }

    public void setStatus(LoanInstallmentStatus status) {
        this.status = status;
    }
}
