package com.xdev.ooms.conditioning.analytics.dto;

import java.math.BigDecimal;

public class GlobalOfReportDto {
    private long totalOf;
    private long plannedOf;
    private long inProgressOf;
    private long completedOf;
    private long canceledOf;
    private BigDecimal totalTargetQuantity = BigDecimal.ZERO;
    private BigDecimal totalProducedQuantity = BigDecimal.ZERO;

    public long getTotalOf() {
        return totalOf;
    }

    public long getPlannedOf() {
        return plannedOf;
    }

    public long getInProgressOf() {
        return inProgressOf;
    }

    public long getCompletedOf() {
        return completedOf;
    }

    public long getCanceledOf() {
        return canceledOf;
    }

    public BigDecimal getTotalTargetQuantity() {
        return totalTargetQuantity;
    }

    public BigDecimal getTotalProducedQuantity() {
        return totalProducedQuantity;
    }

    public void setTotalOf(long totalOf) {
        this.totalOf = totalOf;
    }

    public void setPlannedOf(long plannedOf) {
        this.plannedOf = plannedOf;
    }

    public void setInProgressOf(long inProgressOf) {
        this.inProgressOf = inProgressOf;
    }

    public void setCompletedOf(long completedOf) {
        this.completedOf = completedOf;
    }

    public void setCanceledOf(long canceledOf) {
        this.canceledOf = canceledOf;
    }

    public void setTotalTargetQuantity(BigDecimal totalTargetQuantity) {
        this.totalTargetQuantity = totalTargetQuantity;
    }

    public void setTotalProducedQuantity(BigDecimal totalProducedQuantity) {
        this.totalProducedQuantity = totalProducedQuantity;
    }
}
