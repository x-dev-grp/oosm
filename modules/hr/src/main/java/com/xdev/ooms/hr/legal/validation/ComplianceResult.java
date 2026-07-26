package com.xdev.ooms.hr.legal.validation;

import java.math.BigDecimal;

public class ComplianceResult {

    private ComplianceStatus status;
    private BigDecimal applicableMinimum;
    private String message;

    public ComplianceResult() {
    }

    public ComplianceResult(ComplianceStatus status, BigDecimal applicableMinimum, String message) {
        this.status = status;
        this.applicableMinimum = applicableMinimum;
        this.message = message;
    }

    public static ComplianceResult compliant(BigDecimal applicableMinimum) {
        return new ComplianceResult(ComplianceStatus.COMPLIANT, applicableMinimum, null);
    }

    public static ComplianceResult nonCompliant(BigDecimal applicableMinimum, String message) {
        return new ComplianceResult(ComplianceStatus.NON_COMPLIANT, applicableMinimum, message);
    }

    public ComplianceStatus getStatus() {
        return status;
    }

    public void setStatus(ComplianceStatus status) {
        this.status = status;
    }

    public BigDecimal getApplicableMinimum() {
        return applicableMinimum;
    }

    public void setApplicableMinimum(BigDecimal applicableMinimum) {
        this.applicableMinimum = applicableMinimum;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isCompliant() {
        return status == ComplianceStatus.COMPLIANT;
    }
}
