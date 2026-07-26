package com.xdev.ooms.hr.compliance.dto;

public class ComplianceSummaryDto {

    private int score;
    private long totalOpen;
    private long criticalCount;
    private long highCount;
    private long warningCount;
    private long infoCount;
    private long resolvedCount;
    private long dismissedCount;

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public long getTotalOpen() {
        return totalOpen;
    }

    public void setTotalOpen(long totalOpen) {
        this.totalOpen = totalOpen;
    }

    /** Alias for frontend KPI binding. */
    public long getOpenCount() {
        return totalOpen;
    }

    public long getCriticalCount() {
        return criticalCount;
    }

    public void setCriticalCount(long criticalCount) {
        this.criticalCount = criticalCount;
    }

    public long getHighCount() {
        return highCount;
    }

    public void setHighCount(long highCount) {
        this.highCount = highCount;
    }

    public long getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(long warningCount) {
        this.warningCount = warningCount;
    }

    public long getInfoCount() {
        return infoCount;
    }

    public void setInfoCount(long infoCount) {
        this.infoCount = infoCount;
    }

    public long getResolvedCount() {
        return resolvedCount;
    }

    public void setResolvedCount(long resolvedCount) {
        this.resolvedCount = resolvedCount;
    }

    public long getDismissedCount() {
        return dismissedCount;
    }

    public void setDismissedCount(long dismissedCount) {
        this.dismissedCount = dismissedCount;
    }
}
