package com.xdev.ooms.hr.schedule.dto;

import com.xdev.ooms.hr.schedule.entity.WorkSchedule;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.math.BigDecimal;
import java.time.LocalTime;

public class WorkScheduleDto extends BaseDto<WorkSchedule> {
    private String name;
    private BigDecimal weeklyHours;
    private String workingDays;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer breakDurationMinutes;
    private Boolean nightShift;
    private Boolean rotatingShift;
    private Boolean active;
    private String scheduleCode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getWeeklyHours() {
        return weeklyHours;
    }

    public void setWeeklyHours(BigDecimal weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    public String getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(String workingDays) {
        this.workingDays = workingDays;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getBreakDurationMinutes() {
        return breakDurationMinutes;
    }

    public void setBreakDurationMinutes(Integer breakDurationMinutes) {
        this.breakDurationMinutes = breakDurationMinutes;
    }

    public Boolean getNightShift() {
        return nightShift;
    }

    public void setNightShift(Boolean nightShift) {
        this.nightShift = nightShift;
    }

    public Boolean getRotatingShift() {
        return rotatingShift;
    }

    public void setRotatingShift(Boolean rotatingShift) {
        this.rotatingShift = rotatingShift;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getScheduleCode() {
        return scheduleCode;
    }

    public void setScheduleCode(String scheduleCode) {
        this.scheduleCode = scheduleCode;
    }
}
