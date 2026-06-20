package com.xdev.ooms.conditioning.analytics.dto;

import java.math.BigDecimal;

public class QualityReportDto {
    private String productName;
    private long totalControls;
    private long failedControls;
    private BigDecimal nonConformityRate;

    public String getProductName() {
        return productName;
    }

    public long getTotalControls() {
        return totalControls;
    }

    public long getFailedControls() {
        return failedControls;
    }

    public BigDecimal getNonConformityRate() {
        return nonConformityRate;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setTotalControls(long totalControls) {
        this.totalControls = totalControls;
    }

    public void setFailedControls(long failedControls) {
        this.failedControls = failedControls;
    }

    public void setNonConformityRate(BigDecimal nonConformityRate) {
        this.nonConformityRate = nonConformityRate;
    }
}
