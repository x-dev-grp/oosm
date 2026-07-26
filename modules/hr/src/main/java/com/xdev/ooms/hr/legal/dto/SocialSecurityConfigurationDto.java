package com.xdev.ooms.hr.legal.dto;

import com.xdev.ooms.hr.legal.entity.SocialSecurityConfiguration;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SocialSecurityConfigurationDto extends BaseDto<SocialSecurityConfiguration> {
    private String regime;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private BigDecimal employeeRate;
    private BigDecimal employerRate;
    private BigDecimal cssRate;
    private BigDecimal accidentContributionRate;
    private String calculationBaseRule;
    private String legalReference;
    private Integer version;
    private Boolean active;

    public String getRegime() {
        return regime;
    }

    public void setRegime(String regime) {
        this.regime = regime;
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

    public BigDecimal getEmployeeRate() {
        return employeeRate;
    }

    public void setEmployeeRate(BigDecimal employeeRate) {
        this.employeeRate = employeeRate;
    }

    public BigDecimal getEmployerRate() {
        return employerRate;
    }

    public void setEmployerRate(BigDecimal employerRate) {
        this.employerRate = employerRate;
    }

    public BigDecimal getCssRate() {
        return cssRate;
    }

    public void setCssRate(BigDecimal cssRate) {
        this.cssRate = cssRate;
    }

    public BigDecimal getAccidentContributionRate() {
        return accidentContributionRate;
    }

    public void setAccidentContributionRate(BigDecimal accidentContributionRate) {
        this.accidentContributionRate = accidentContributionRate;
    }

    public String getCalculationBaseRule() {
        return calculationBaseRule;
    }

    public void setCalculationBaseRule(String calculationBaseRule) {
        this.calculationBaseRule = calculationBaseRule;
    }

    public String getLegalReference() {
        return legalReference;
    }

    public void setLegalReference(String legalReference) {
        this.legalReference = legalReference;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
