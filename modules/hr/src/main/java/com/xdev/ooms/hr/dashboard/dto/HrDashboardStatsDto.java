package com.xdev.ooms.hr.dashboard.dto;

public class HrDashboardStatsDto {

    private long totalEmployees;
    private long activeEmployees;
    private long presentToday;
    private long absentToday;
    private long onLeave;
    private long pendingLeaveRequests;
    private long contractsExpiringSoon;
    private long attendanceAnomalies;
    private String payrollStatus;
    private int complianceScore;
    private long criticalComplianceIssues;

    public long getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(long totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public long getActiveEmployees() {
        return activeEmployees;
    }

    public void setActiveEmployees(long activeEmployees) {
        this.activeEmployees = activeEmployees;
    }

    public long getPresentToday() {
        return presentToday;
    }

    public void setPresentToday(long presentToday) {
        this.presentToday = presentToday;
    }

    public long getAbsentToday() {
        return absentToday;
    }

    public void setAbsentToday(long absentToday) {
        this.absentToday = absentToday;
    }

    public long getOnLeave() {
        return onLeave;
    }

    public void setOnLeave(long onLeave) {
        this.onLeave = onLeave;
    }

    public long getPendingLeaveRequests() {
        return pendingLeaveRequests;
    }

    public void setPendingLeaveRequests(long pendingLeaveRequests) {
        this.pendingLeaveRequests = pendingLeaveRequests;
    }

    public long getContractsExpiringSoon() {
        return contractsExpiringSoon;
    }

    public void setContractsExpiringSoon(long contractsExpiringSoon) {
        this.contractsExpiringSoon = contractsExpiringSoon;
    }

    public long getAttendanceAnomalies() {
        return attendanceAnomalies;
    }

    public void setAttendanceAnomalies(long attendanceAnomalies) {
        this.attendanceAnomalies = attendanceAnomalies;
    }

    public String getPayrollStatus() {
        return payrollStatus;
    }

    public void setPayrollStatus(String payrollStatus) {
        this.payrollStatus = payrollStatus;
    }

    public int getComplianceScore() {
        return complianceScore;
    }

    public void setComplianceScore(int complianceScore) {
        this.complianceScore = complianceScore;
    }

    public long getCriticalComplianceIssues() {
        return criticalComplianceIssues;
    }

    public void setCriticalComplianceIssues(long criticalComplianceIssues) {
        this.criticalComplianceIssues = criticalComplianceIssues;
    }
}
