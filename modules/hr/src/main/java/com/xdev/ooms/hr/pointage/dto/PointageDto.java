package com.xdev.ooms.hr.pointage.dto;

import com.xdev.ooms.hr.common.enums.AttendanceStatus;
import com.xdev.ooms.hr.employee.dto.EmployeeDto;
import com.xdev.ooms.hr.pointage.entity.Pointage;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.time.LocalDate;
import java.time.LocalTime;

public class PointageDto extends BaseDto<Pointage> {
    private EmployeeDto employee;
    private LocalDate workDate;
    private LocalTime checkIn;
    private LocalTime checkOut;
    private Double workedHours;
    private Integer breakMinutes;
    private AttendanceStatus status;
    private String notes;

    public EmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeDto employee) {
        this.employee = employee;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public LocalTime getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(LocalTime checkIn) {
        this.checkIn = checkIn;
    }

    public LocalTime getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(LocalTime checkOut) {
        this.checkOut = checkOut;
    }

    public Double getWorkedHours() {
        return workedHours;
    }

    public void setWorkedHours(Double workedHours) {
        this.workedHours = workedHours;
    }

    public Integer getBreakMinutes() {
        return breakMinutes;
    }

    public void setBreakMinutes(Integer breakMinutes) {
        this.breakMinutes = breakMinutes;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
