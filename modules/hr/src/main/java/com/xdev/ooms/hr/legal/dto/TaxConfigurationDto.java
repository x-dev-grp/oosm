package com.xdev.ooms.hr.legal.dto;

import com.xdev.ooms.hr.legal.entity.TaxConfiguration;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.time.LocalDate;
import java.util.List;

public class TaxConfigurationDto extends BaseDto<TaxConfiguration> {
    private Integer fiscalYear;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String description;
    private Integer version;
    private Boolean active;
    private List<TaxBracketDto> brackets;

    public Integer getFiscalYear() {
        return fiscalYear;
    }

    public void setFiscalYear(Integer fiscalYear) {
        this.fiscalYear = fiscalYear;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public List<TaxBracketDto> getBrackets() {
        return brackets;
    }

    public void setBrackets(List<TaxBracketDto> brackets) {
        this.brackets = brackets;
    }
}
