package com.xdev.ooms.conditioning.analytics.dto;

import java.math.BigDecimal;

public class BomGapDto {
    private String materialName;
    private BigDecimal plannedQuantity;
    private BigDecimal actualQuantity;
    private BigDecimal gapQuantity;
    private BigDecimal gapPercentage;

    public String getMaterialName() {
        return materialName;
    }

    public BigDecimal getPlannedQuantity() {
        return plannedQuantity;
    }

    public BigDecimal getActualQuantity() {
        return actualQuantity;
    }

    public BigDecimal getGapQuantity() {
        return gapQuantity;
    }

    public BigDecimal getGapPercentage() {
        return gapPercentage;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public void setPlannedQuantity(BigDecimal plannedQuantity) {
        this.plannedQuantity = plannedQuantity;
    }

    public void setActualQuantity(BigDecimal actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public void setGapQuantity(BigDecimal gapQuantity) {
        this.gapQuantity = gapQuantity;
    }

    public void setGapPercentage(BigDecimal gapPercentage) {
        this.gapPercentage = gapPercentage;
    }
}
