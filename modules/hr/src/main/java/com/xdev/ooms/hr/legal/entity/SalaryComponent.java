package com.xdev.ooms.hr.legal.entity;

import com.xdev.ooms.hr.legal.enums.CalculationType;
import com.xdev.ooms.hr.legal.enums.ComponentType;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "hr_salary_component")
public class SalaryComponent extends BaseEntity implements Serializable {

    @Column(nullable = false)
    private String code;

    private String labelFr;

    private String labelAr;

    @Enumerated(EnumType.STRING)
    private ComponentType type;

    @Enumerated(EnumType.STRING)
    private CalculationType calculationType;

    private Boolean taxable;

    private Boolean cnssApplicable;

    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    private Boolean active = true;

    @Column(columnDefinition = "TEXT")
    private String formulaOrConfig;

    private Integer sortOrder;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabelFr() {
        return labelFr;
    }

    public void setLabelFr(String labelFr) {
        this.labelFr = labelFr;
    }

    public String getLabelAr() {
        return labelAr;
    }

    public void setLabelAr(String labelAr) {
        this.labelAr = labelAr;
    }

    public ComponentType getType() {
        return type;
    }

    public void setType(ComponentType type) {
        this.type = type;
    }

    public CalculationType getCalculationType() {
        return calculationType;
    }

    public void setCalculationType(CalculationType calculationType) {
        this.calculationType = calculationType;
    }

    public Boolean getTaxable() {
        return taxable;
    }

    public void setTaxable(Boolean taxable) {
        this.taxable = taxable;
    }

    public Boolean getCnssApplicable() {
        return cnssApplicable;
    }

    public void setCnssApplicable(Boolean cnssApplicable) {
        this.cnssApplicable = cnssApplicable;
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

    public String getFormulaOrConfig() {
        return formulaOrConfig;
    }

    public void setFormulaOrConfig(String formulaOrConfig) {
        this.formulaOrConfig = formulaOrConfig;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
