package com.xdev.ooms.hr.payroll.entity;

import com.xdev.ooms.hr.common.enums.PayrollPeriodStatus;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "hr_payroll_period")
public class PayrollPeriod extends BaseEntity implements Serializable {

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private LocalDate periodStart;

    @Column(nullable = false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    private PayrollPeriodStatus status;

    /** True when this period has been posted to finance (idempotency). */
    private Boolean financePosted = Boolean.FALSE;

    /** Invoice / finance reference returned by PayrollAccountingPort. */
    @Column(length = 128)
    private String financeReference;

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public PayrollPeriodStatus getStatus() {
        return status;
    }

    public void setStatus(PayrollPeriodStatus status) {
        this.status = status;
    }

    public Boolean getFinancePosted() {
        return financePosted;
    }

    public void setFinancePosted(Boolean financePosted) {
        this.financePosted = financePosted;
    }

    public String getFinanceReference() {
        return financeReference;
    }

    public void setFinanceReference(String financeReference) {
        this.financeReference = financeReference;
    }
}
