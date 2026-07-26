package com.xdev.ooms.hr.timesheet.dto;

import com.xdev.ooms.hr.pointage.dto.PointageDto;
import com.xdev.ooms.hr.timesheet.entity.TimesheetLine;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.time.LocalDate;

public class TimesheetLineDto extends BaseDto<TimesheetLine> {
    private TimesheetDto timesheet;
    private LocalDate date;
    private Integer workedMinutes;
    private Integer overtimeMinutes;
    private PointageDto pointage;
    private String notes;

    public TimesheetDto getTimesheet() {
        return timesheet;
    }

    public void setTimesheet(TimesheetDto timesheet) {
        this.timesheet = timesheet;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getWorkedMinutes() {
        return workedMinutes;
    }

    public void setWorkedMinutes(Integer workedMinutes) {
        this.workedMinutes = workedMinutes;
    }

    public Integer getOvertimeMinutes() {
        return overtimeMinutes;
    }

    public void setOvertimeMinutes(Integer overtimeMinutes) {
        this.overtimeMinutes = overtimeMinutes;
    }

    public PointageDto getPointage() {
        return pointage;
    }

    public void setPointage(PointageDto pointage) {
        this.pointage = pointage;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
