package com.xdev.ooms.hr.timesheet.entity;

import com.xdev.ooms.hr.common.enums.TimesheetStatus;
import com.xdev.ooms.hr.employee.entity.Employee;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hr_timesheet")
public class Timesheet extends BaseEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private Integer periodYear;

    @Column(nullable = false)
    private Integer periodMonth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimesheetStatus status = TimesheetStatus.DRAFT;

    private Integer totalWorkedMinutes;
    private Integer totalOvertimeMinutes;
    private LocalDateTime validatedAt;
    private String validatedBy;

    @OneToMany(mappedBy = "timesheet", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
    @OrderBy("date ASC")
    private List<TimesheetLine> lines = new ArrayList<>();

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
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

    public List<TimesheetLine> getLines() {
        return lines;
    }

    public void setLines(List<TimesheetLine> lines) {
        this.lines = lines;
    }
}
