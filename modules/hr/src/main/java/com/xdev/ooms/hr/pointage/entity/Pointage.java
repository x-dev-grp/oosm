package com.xdev.ooms.hr.pointage.entity;

import com.xdev.ooms.hr.common.enums.AttendanceStatus;
import com.xdev.ooms.hr.employee.entity.Employee;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "hr_pointage")
public class Pointage extends BaseEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate workDate;

    private LocalTime checkIn;
    private LocalTime checkOut;
    private Double workedHours;
    private Integer breakMinutes;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
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
