package com.xdev.ooms.production.equipment.dto;

import com.xdev.ooms.production.equipment.entity.MillEquipment;
import com.xdev.ooms.production.equipment.enums.MillEquipmentStatus;
import com.xdev.ooms.production.equipment.enums.MillEquipmentType;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.time.LocalDateTime;

public class MillEquipmentDto extends BaseDto<MillEquipment> {

    private String code;
    private String name;
    private MillEquipmentType equipmentType;
    private String registrationNumber;
    private Double defaultHourlyRate;
    private MillEquipmentStatus status;
    private Double hoursOperated;
    private LocalDateTime lastMaintenanceDate;
    private LocalDateTime nextMaintenanceDate;
    private String notes;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MillEquipmentType getEquipmentType() {
        return equipmentType;
    }

    public void setEquipmentType(MillEquipmentType equipmentType) {
        this.equipmentType = equipmentType;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public Double getDefaultHourlyRate() {
        return defaultHourlyRate;
    }

    public void setDefaultHourlyRate(Double defaultHourlyRate) {
        this.defaultHourlyRate = defaultHourlyRate;
    }

    public MillEquipmentStatus getStatus() {
        return status;
    }

    public void setStatus(MillEquipmentStatus status) {
        this.status = status;
    }

    public Double getHoursOperated() {
        return hoursOperated;
    }

    public void setHoursOperated(Double hoursOperated) {
        this.hoursOperated = hoursOperated;
    }

    public LocalDateTime getLastMaintenanceDate() {
        return lastMaintenanceDate;
    }

    public void setLastMaintenanceDate(LocalDateTime lastMaintenanceDate) {
        this.lastMaintenanceDate = lastMaintenanceDate;
    }

    public LocalDateTime getNextMaintenanceDate() {
        return nextMaintenanceDate;
    }

    public void setNextMaintenanceDate(LocalDateTime nextMaintenanceDate) {
        this.nextMaintenanceDate = nextMaintenanceDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
