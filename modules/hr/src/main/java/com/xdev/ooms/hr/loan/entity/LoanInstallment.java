package com.xdev.ooms.hr.loan.entity;

import com.xdev.ooms.hr.common.enums.LoanInstallmentStatus;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "hr_loan_installment")
public class LoanInstallment extends BaseEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private EmployeeLoan loan;

    private LocalDate dueDate;

    @Column(precision = 19, scale = 3)
    private BigDecimal amount;

    @Column(precision = 19, scale = 3)
    private BigDecimal paidAmount;

    private Integer payrollPeriodYear;
    private Integer payrollPeriodMonth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanInstallmentStatus status = LoanInstallmentStatus.PENDING;

    public EmployeeLoan getLoan() {
        return loan;
    }

    public void setLoan(EmployeeLoan loan) {
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
