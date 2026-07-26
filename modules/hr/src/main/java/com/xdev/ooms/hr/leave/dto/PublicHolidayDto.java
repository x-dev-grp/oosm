package com.xdev.ooms.hr.leave.dto;

import com.xdev.ooms.hr.leave.entity.PublicHoliday;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.time.LocalDate;

public class PublicHolidayDto extends BaseDto<PublicHoliday> {
    private LocalDate date;
    private String nameFr;
    private String nameAr;
    private Boolean paid;
    private Boolean legal;
    private Boolean recurring;
    private Integer effectiveYear;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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

    public Boolean getLegal() {
        return legal;
    }

    public void setLegal(Boolean legal) {
        this.legal = legal;
    }

    public Boolean getRecurring() {
        return recurring;
    }

    public void setRecurring(Boolean recurring) {
        this.recurring = recurring;
    }

    public Integer getEffectiveYear() {
        return effectiveYear;
    }

    public void setEffectiveYear(Integer effectiveYear) {
        this.effectiveYear = effectiveYear;
    }
}
