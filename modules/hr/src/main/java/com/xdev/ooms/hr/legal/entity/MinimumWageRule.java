package com.xdev.ooms.hr.legal.entity;

import com.xdev.ooms.hr.legal.enums.WeeklyRegimeType;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "hr_minimum_wage_rule")
public class MinimumWageRule extends BaseEntity implements Serializable {

    private String profile;

    private String sector;

    @Enumerated(EnumType.STRING)
    private WeeklyRegimeType weeklyRegime;

    @Column(precision = 19, scale = 3)
    private BigDecimal monthlyMinimum;

    @Column(precision = 19, scale = 3)
    private BigDecimal hourlyMinimum;

    @Column(precision = 19, scale = 3)
    private BigDecimal dailyMinimum;

    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    private String legalReference;

    private Boolean active = true;

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
