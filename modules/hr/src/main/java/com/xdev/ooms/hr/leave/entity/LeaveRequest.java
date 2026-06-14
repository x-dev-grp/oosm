package com.xdev.ooms.hr.leave.entity;

import com.xdev.ooms.sharedkernel.Enum.LeaveStatus;
import com.xdev.ooms.sharedkernel.Enum.LeaveType;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
public class LeaveRequest extends BaseEntity implements Serializable {

    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private long duration_days;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status;
    @Enumerated(EnumType.STRING)
    private LeaveType leaveType;


    public long getDuration_days() {
        return duration_days;
    }

    public void setDuration_days(long duration_days) {
        this.duration_days = duration_days;
    }



    // Getters et Setters
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LeaveStatus getStatus() {
        return status;
    }

    public void setStatus(LeaveStatus status) {
        this.status = status;
    }
}
