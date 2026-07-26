package com.xdev.ooms.hr.timesheet.dto;

import com.xdev.ooms.hr.common.enums.TimesheetStatus;
import com.xdev.ooms.hr.employee.dto.EmployeeDto;
import com.xdev.ooms.hr.timesheet.entity.Timesheet;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.time.LocalDateTime;
import java.util.List;

public class TimesheetDto extends BaseDto<Timesheet> {
    private EmployeeDto employee;
    private Integer periodYear;
    private Integer periodMonth;
    private TimesheetStatus status;
    private Integer totalWorkedMinutes;
    private Integer totalOvertimeMinutes;
    private LocalDateTime validatedAt;
    private String validatedBy;
    private List<TimesheetLineDto> lines;

    public EmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeDto employee) {
        this.employee = employee;
    }

    public Integer getPeriodYear() {
        return periodYear;
    }

    public void setPeriodYear(Integer periodYear) {
        this.periodYear = periodYear;
    }

    public Integer getPeriodMonth() {
        return periodMonth;
    }

    public void setPeriodMonth(Integer periodMonth) {
        this.periodMonth = periodMonth;
    }

    public TimesheetStatus getStatus() {
        return status;
    }

    public void setStatus(TimesheetStatus status) {
        this.status = status;
    }

    public Integer getTotalWorkedMinutes() {
        return totalWorkedMinutes;
    }

    public void setTotalWorkedMinutes(Integer totalWorkedMinutes) {
        this.totalWorkedMinutes = totalWorkedMinutes;
    }

    public Integer getTotalOvertimeMinutes() {
        return totalOvertimeMinutes;
    }

    public void setTotalOvertimeMinutes(Integer totalOvertimeMinutes) {
        this.totalOvertimeMinutes = totalOvertimeMinutes;
    }

    public LocalDateTime getValidatedAt() {
        return validatedAt;
    }

    public void setValidatedAt(LocalDateTime validatedAt) {
        this.validatedAt = validatedAt;
    }

    public String getValidatedBy() {
        return validatedBy;
    }

    public void setValidatedBy(String validatedBy) {
        this.validatedBy = validatedBy;
    }

    public List<TimesheetLineDto> getLines() {
        return lines;
    }

    public void setLines(List<TimesheetLineDto> lines) {
        this.lines = lines;
    }
}
