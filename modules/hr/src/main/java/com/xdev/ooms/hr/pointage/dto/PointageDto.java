package com.xdev.ooms.hr.pointage.dto;

import com.xdev.ooms.sharedkernel.Enum.PointageStatus;
import com.xdev.ooms.hr.pointage.entity.Pointage;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDate;
import java.time.LocalTime;

public class PointageDto extends BaseDto<Pointage> {
    private LocalDate date;
    private LocalTime checkIn;
    private LocalTime checkOut;
    @Enumerated(EnumType.STRING)
    private PointageStatus status;
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
    }

    public LocalTime getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(LocalTime checkOut) {
        this.checkOut = checkOut;
    }

    public PointageStatus getStatus() {
        return status;
    }

    public void setStatus(PointageStatus status) {
        this.status = status;
    }
}
