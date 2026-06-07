package com.xdev.ooms.hr.Dtos;

import com.xdev.ooms.sharedkernel.Enum.LeaveStatus;
import com.xdev.ooms.sharedkernel.Enum.LeaveType;
import com.xdev.ooms.hr.model.LeaveRequest;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDate;

public class LeaveRequestDto extends BaseDto<LeaveRequest> {
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status;
    @Enumerated(EnumType.STRING)
    private LeaveType leaveType;

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

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }
}
