package com.xdev.ooms.hr.leave.entity;

import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "hr_leave_type")
public class LeaveTypeConfig extends BaseEntity implements Serializable {

    @Column(nullable = false, length = 64)
    private String code;

    private String nameFr;
    private String nameAr;
    private Boolean paid = true;
    private Boolean requiresApproval = true;
    private Boolean requiresDocument = false;
    private Boolean impactsPayroll = true;
    private Boolean impactsSeniority = true;

    @Column(precision = 19, scale = 3)
    private BigDecimal annualAllowance;

    private String carryForwardPolicy;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean active = true;

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
