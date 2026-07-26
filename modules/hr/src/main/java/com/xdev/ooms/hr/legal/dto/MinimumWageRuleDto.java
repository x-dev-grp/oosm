package com.xdev.ooms.hr.legal.dto;

import com.xdev.ooms.hr.legal.entity.MinimumWageRule;
import com.xdev.ooms.hr.legal.enums.WeeklyRegimeType;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MinimumWageRuleDto extends BaseDto<MinimumWageRule> {
    private String profile;
    private String sector;
    private WeeklyRegimeType weeklyRegime;
    private BigDecimal monthlyMinimum;
    private BigDecimal hourlyMinimum;
    private BigDecimal dailyMinimum;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String legalReference;
    private Boolean active;

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public WeeklyRegimeType getWeeklyRegime() {
        return weeklyRegime;
    }

    public void setWeeklyRegime(WeeklyRegimeType weeklyRegime) {
        this.weeklyRegime = weeklyRegime;
    }

    public BigDecimal getMonthlyMinimum() {
        return monthlyMinimum;
    }

    public void setMonthlyMinimum(BigDecimal monthlyMinimum) {
        this.monthlyMinimum = monthlyMinimum;
    }

    public BigDecimal getHourlyMinimum() {
        return hourlyMinimum;
    }

    public void setHourlyMinimum(BigDecimal hourlyMinimum) {
        this.hourlyMinimum = hourlyMinimum;
    }

    public BigDecimal getDailyMinimum() {
        return dailyMinimum;
    }

    public void setDailyMinimum(BigDecimal dailyMinimum) {
        this.dailyMinimum = dailyMinimum;
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

    public String getLegalReference() {
        return legalReference;
    }

    public void setLegalReference(String legalReference) {
        this.legalReference = legalReference;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
