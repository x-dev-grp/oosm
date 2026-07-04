package com.xdev.ooms.hr.leave.dto;

import com.xdev.ooms.hr.common.enums.LeaveStatus;
import com.xdev.ooms.hr.common.enums.LeaveType;
import com.xdev.ooms.hr.employee.dto.EmployeeDto;
import com.xdev.ooms.hr.leave.entity.LeaveRequest;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.time.LocalDate;

public class LeaveRequestDto extends BaseDto<LeaveRequest> {
    private EmployeeDto employee;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double durationDays;
    private LeaveStatus status;

    public EmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeDto employee) {
        this.employee = employee;
    }

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Double getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Double durationDays) {
        this.durationDays = durationDays;
    }

    public LeaveStatus getStatus() {
        return status;
    }

    public void setStatus(LeaveStatus status) {
        this.status = status;
    }
}
