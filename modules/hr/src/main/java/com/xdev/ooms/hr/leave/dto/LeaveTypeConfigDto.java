package com.xdev.ooms.hr.leave.dto;

import com.xdev.ooms.hr.leave.entity.LeaveTypeConfig;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LeaveTypeConfigDto extends BaseDto<LeaveTypeConfig> {
    private String code;
    private String nameFr;
    private String nameAr;
    private Boolean paid;
    private Boolean requiresApproval;
    private Boolean requiresDocument;
    private Boolean impactsPayroll;
    private Boolean impactsSeniority;
    private BigDecimal annualAllowance;
    private String carryForwardPolicy;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean active;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNameFr() {
        return nameFr;
    }

    public void setNameFr(String nameFr) {
        this.nameFr = nameFr;
    }

    public String getNameAr() {
        return nameAr;
    }

    public void setNameAr(String nameAr) {
        this.nameAr = nameAr;
    }

    public Boolean getPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public Boolean getRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(Boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    public Boolean getRequiresDocument() {
        return requiresDocument;
    }

    public void setRequiresDocument(Boolean requiresDocument) {
        this.requiresDocument = requiresDocument;
    }

    public Boolean getImpactsPayroll() {
        return impactsPayroll;
    }

    public void setImpactsPayroll(Boolean impactsPayroll) {
        this.impactsPayroll = impactsPayroll;
    }

    public Boolean getImpactsSeniority() {
        return impactsSeniority;
    }

    public void setImpactsSeniority(Boolean impactsSeniority) {
        this.impactsSeniority = impactsSeniority;
    }

    public BigDecimal getAnnualAllowance() {
        return annualAllowance;
    }

    public void setAnnualAllowance(BigDecimal annualAllowance) {
        this.annualAllowance = annualAllowance;
    }

    public String getCarryForwardPolicy() {
        return carryForwardPolicy;
    }

    public void setCarryForwardPolicy(String carryForwardPolicy) {
        this.carryForwardPolicy = carryForwardPolicy;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
