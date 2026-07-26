package com.xdev.ooms.hr.legal.dto;

import com.xdev.ooms.hr.legal.entity.TaxBracket;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.math.BigDecimal;

public class TaxBracketDto extends BaseDto<TaxBracket> {
    private TaxConfigurationDto taxConfiguration;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal rate;
    private BigDecimal fixedAmount;
    private Integer sortOrder;

    public TaxConfigurationDto getTaxConfiguration() {
        return taxConfiguration;
    }

    public void setTaxConfiguration(TaxConfigurationDto taxConfiguration) {
        this.taxConfiguration = taxConfiguration;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getFixedAmount() {
        return fixedAmount;
    }

    public void setFixedAmount(BigDecimal fixedAmount) {
        this.fixedAmount = fixedAmount;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
