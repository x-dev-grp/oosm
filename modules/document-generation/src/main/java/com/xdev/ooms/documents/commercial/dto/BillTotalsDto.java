package com.xdev.ooms.documents.commercial.dto;

import java.math.BigDecimal;

public class BillTotalsDto {
    private BigDecimal subtotalExcludingVat = BigDecimal.ZERO;
    private BigDecimal vatAmount = BigDecimal.ZERO;
    private BigDecimal totalIncludingVat = BigDecimal.ZERO;

    public BigDecimal getSubtotalExcludingVat() {
        return subtotalExcludingVat;
    }

    public void setSubtotalExcludingVat(BigDecimal subtotalExcludingVat) {
        this.subtotalExcludingVat = subtotalExcludingVat;
    }

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(BigDecimal vatAmount) {
        this.vatAmount = vatAmount;
    }

    public BigDecimal getTotalIncludingVat() {
        return totalIncludingVat;
    }

    public void setTotalIncludingVat(BigDecimal totalIncludingVat) {
        this.totalIncludingVat = totalIncludingVat;
    }
}
