package com.xdev.ooms.hr.leave.entity;

import com.xdev.ooms.hr.common.enums.LeaveStatus;
import com.xdev.ooms.hr.common.enums.LeaveType;
import com.xdev.ooms.hr.employee.entity.Employee;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "hr_leave_request")
public class LeaveRequest extends BaseEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType leaveType;
    @Column(nullable = false)
    private LocalDate startDate;
    @Column(nullable = false)
    private LocalDate endDate;
    private Double durationDays;
    @Enumerated(EnumType.STRING)
    private LeaveStatus status;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
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
