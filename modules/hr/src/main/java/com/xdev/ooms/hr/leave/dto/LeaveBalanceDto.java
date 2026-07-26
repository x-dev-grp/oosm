package com.xdev.ooms.hr.leave.dto;

import com.xdev.ooms.hr.employee.dto.EmployeeDto;
import com.xdev.ooms.hr.leave.entity.LeaveBalance;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.math.BigDecimal;

public class LeaveBalanceDto extends BaseDto<LeaveBalance> {
    private EmployeeDto employee;
    private String leaveTypeCode;
    private Integer year;
    private BigDecimal entitled;
    private BigDecimal taken;
    private BigDecimal pending;
    private BigDecimal balance;

    public EmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeDto employee) {
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
