package com.xdev.ooms.hr.model;

import com.xdev.ooms.sharedkernel.Enum.PointageStatus;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;

import java.io.Serializable;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Pointage extends BaseEntity implements Serializable {

    private LocalDate date;
    private LocalTime checkIn;
    private LocalTime checkOut;

    private Time pointageDuree;

    @Enumerated(EnumType.STRING)
    private PointageStatus status;

    // relation
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    // Getters et Setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(LocalTime checkIn) {
        this.checkIn = checkIn;
        updateDuration();
    }

    public LocalTime getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(LocalTime checkOut) {
        this.checkOut = checkOut;
        updateDuration();
    }

    public Time getPointageDuree() {
        return pointageDuree;
    }

    private void updateDuration() {
        if (checkIn != null && checkOut != null) {
            Duration duration = Duration.between(checkIn, checkOut);
            // Ensure positive duration (checkOut after checkIn)
            if (!duration.isNegative()) {
                this.pointageDuree = new Time(duration.toMillis());
            } else {
                this.pointageDuree = null;
            }
        }
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public PointageStatus getStatus() {
        return status;
    }

    public void setStatus(PointageStatus status) {
        this.status = status;
    }
}
