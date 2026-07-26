package com.xdev.ooms.hr.overtime.dto;

import com.xdev.ooms.hr.common.enums.OvertimeRequestStatus;
import com.xdev.ooms.hr.employee.dto.EmployeeDto;
import com.xdev.ooms.hr.overtime.entity.OvertimeRequest;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class OvertimeRequestDto extends BaseDto<OvertimeRequest> {
    private EmployeeDto employee;
    private LocalDate date;
    private Integer minutes;
    private String reason;
    private OvertimeRequestStatus status;
    private BigDecimal multiplierApplied;
    private BigDecimal amount;

    public EmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeDto employee) {
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
