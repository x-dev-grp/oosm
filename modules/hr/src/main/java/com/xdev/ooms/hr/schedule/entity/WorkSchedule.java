package com.xdev.ooms.hr.schedule.entity;

import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "hr_work_schedule")
public class WorkSchedule extends BaseEntity implements Serializable {

    @Column(nullable = false)
    private String name;

    @Column(precision = 19, scale = 3)
    private BigDecimal weeklyHours;

    /** Comma-separated days e.g. MON,TUE,WED,THU,FRI or JSON list */
    @Column(length = 255)
    private String workingDays;

    private LocalTime startTime;
    private LocalTime endTime;
    private Integer breakDurationMinutes;
    private Boolean nightShift = false;
    private Boolean rotatingShift = false;
    private Boolean active = true;

    /** e.g. ADMIN, SHIFT_A, SHIFT_B, SHIFT_C, OLIVE_CAMPAIGN */
    @Column(length = 64)
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
