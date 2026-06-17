package com.xdev.ooms.finance.billing.dto;

import java.math.BigDecimal;

public class BillLineDto {
    private String designation;
    private BigDecimal quantity = BigDecimal.ONE;
    private String unit = "UNIT";
    private BigDecimal unitPriceExcludingVat = BigDecimal.ZERO;
    private BigDecimal vatRatePercent = BigDecimal.ZERO;

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getUnitPriceExcludingVat() {
        return unitPriceExcludingVat;
    }

    public void setUnitPriceExcludingVat(BigDecimal unitPriceExcludingVat) {
        this.unitPriceExcludingVat = unitPriceExcludingVat;
    }

    public BigDecimal getVatRatePercent() {
        return vatRatePercent;
    }

    public void setVatRatePercent(BigDecimal vatRatePercent) {
        this.vatRatePercent = vatRatePercent;
    }
}
