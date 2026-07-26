package com.xdev.ooms.hr.leave.entity;

import com.xdev.ooms.hr.employee.entity.Employee;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "hr_leave_balance")
public class LeaveBalance extends BaseEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false, length = 64)
    private String leaveTypeCode;

    @Column(nullable = false)
    private Integer year;

    @Column(precision = 19, scale = 3)
    private BigDecimal entitled;

    @Column(precision = 19, scale = 3)
    private BigDecimal taken;

    @Column(precision = 19, scale = 3)
    private BigDecimal pending;

    @Column(precision = 19, scale = 3)
    private BigDecimal balance;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getLeaveTypeCode() {
        return leaveTypeCode;
    }

    public void setLeaveTypeCode(String leaveTypeCode) {
        this.leaveTypeCode = leaveTypeCode;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public BigDecimal getEntitled() {
        return entitled;
    }

    public void setEntitled(BigDecimal entitled) {
        this.entitled = entitled;
    }

    public BigDecimal getTaken() {
        return taken;
    }

    public void setTaken(BigDecimal taken) {
        this.taken = taken;
    }

    public BigDecimal getPending() {
        return pending;
    }

    public void setPending(BigDecimal pending) {
        this.pending = pending;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
