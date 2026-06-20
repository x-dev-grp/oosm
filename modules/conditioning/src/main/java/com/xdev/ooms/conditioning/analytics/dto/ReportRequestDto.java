package com.xdev.ooms.conditioning.analytics.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReportRequestDto {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private UUID productId;
    private UUID ofId;
    private String status;
    private String reportType; // "YIELD", "GLOBAL", "QUALITY", "BOM", "FILTRATION"

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getOfId() {
        return ofId;
    }

    public String getStatus() {
        return status;
    }

    public String getReportType() {
        return reportType;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public void setOfId(UUID ofId) {
        this.ofId = ofId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }
}
