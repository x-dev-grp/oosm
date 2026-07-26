package com.xdev.ooms.hr.advance.entity;

import com.xdev.ooms.hr.common.enums.SalaryAdvanceStatus;
import com.xdev.ooms.hr.employee.entity.Employee;
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
@Table(name = "hr_salary_advance")
public class SalaryAdvance extends BaseEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(precision = 19, scale = 3, nullable = false)
    private BigDecimal amount;

    private LocalDate requestDate;
    private LocalDate paymentDate;
    private Integer deductionPeriodYear;
    private Integer deductionPeriodMonth;

    @Column(precision = 19, scale = 3)
    private BigDecimal remainingAmount;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SalaryAdvanceStatus status = SalaryAdvanceStatus.REQUESTED;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Integer getDeductionPeriodYear() {
        return deductionPeriodYear;
    }

    public void setDeductionPeriodYear(Integer deductionPeriodYear) {
        this.deductionPeriodYear = deductionPeriodYear;
    }

    public Integer getDeductionPeriodMonth() {
        return deductionPeriodMonth;
    }

    public void setDeductionPeriodMonth(Integer deductionPeriodMonth) {
        this.deductionPeriodMonth = deductionPeriodMonth;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public SalaryAdvanceStatus getStatus() {
        return status;
    }

    public void setStatus(SalaryAdvanceStatus status) {
        this.status = status;
    }
}
