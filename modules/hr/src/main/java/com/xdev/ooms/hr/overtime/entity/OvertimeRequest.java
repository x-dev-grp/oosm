package com.xdev.ooms.hr.overtime.entity;

import com.xdev.ooms.hr.common.enums.OvertimeRequestStatus;
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
@Table(name = "hr_overtime_request")
public class OvertimeRequest extends BaseEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate date;

    private Integer minutes;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OvertimeRequestStatus status = OvertimeRequestStatus.REQUESTED;

    @Column(precision = 19, scale = 4)
    private BigDecimal multiplierApplied;

    @Column(precision = 19, scale = 3)
    private BigDecimal amount;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public OvertimeRequestStatus getStatus() {
        return status;
    }

    public void setStatus(OvertimeRequestStatus status) {
        this.status = status;
    }

    public BigDecimal getMultiplierApplied() {
        return multiplierApplied;
    }

    public void setMultiplierApplied(BigDecimal multiplierApplied) {
        this.multiplierApplied = multiplierApplied;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
