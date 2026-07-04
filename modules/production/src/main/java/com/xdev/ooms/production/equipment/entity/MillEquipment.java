package com.xdev.ooms.production.equipment.entity;

import com.xdev.ooms.production.equipment.enums.MillEquipmentStatus;
import com.xdev.ooms.production.equipment.enums.MillEquipmentType;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "mill_equipment")
public class MillEquipment extends BaseEntity {

    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MillEquipmentType equipmentType = MillEquipmentType.OTHER;

    private String registrationNumber;

    private Double defaultHourlyRate = 0d;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MillEquipmentStatus status = MillEquipmentStatus.AVAILABLE;

    private Double hoursOperated = 0d;

    private LocalDateTime lastMaintenanceDate;
    private LocalDateTime nextMaintenanceDate;

    @Column(length = 2000)
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
        this.defaultHourlyRate = defaultHourlyRate == null ? 0d : defaultHourlyRate;
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
        this.hoursOperated = hoursOperated == null ? 0d : hoursOperated;
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
