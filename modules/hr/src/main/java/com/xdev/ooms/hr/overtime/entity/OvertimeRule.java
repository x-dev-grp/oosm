package com.xdev.ooms.hr.overtime.entity;

import com.xdev.ooms.hr.common.enums.OvertimeDayType;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "hr_overtime_rule")
public class OvertimeRule extends BaseEntity implements Serializable {

    @Column(nullable = false, length = 64)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private OvertimeDayType dayType;

    private LocalTime timeRangeStart;
    private LocalTime timeRangeEnd;

    @Column(precision = 19, scale = 4)
    private BigDecimal multiplier;

    @Column(precision = 19, scale = 3)
    private BigDecimal fixedRate;

    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String legalReference;
    private Boolean active = true;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OvertimeDayType getDayType() {
        return dayType;
    }

    public void setDayType(OvertimeDayType dayType) {
        this.dayType = dayType;
    }

    public LocalTime getTimeRangeStart() {
        return timeRangeStart;
    }

    public void setTimeRangeStart(LocalTime timeRangeStart) {
        this.timeRangeStart = timeRangeStart;
    }

    public LocalTime getTimeRangeEnd() {
        return timeRangeEnd;
    }

    public void setTimeRangeEnd(LocalTime timeRangeEnd) {
        this.timeRangeEnd = timeRangeEnd;
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }

    public BigDecimal getFixedRate() {
        return fixedRate;
    }

    public void setFixedRate(BigDecimal fixedRate) {
        this.fixedRate = fixedRate;
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
