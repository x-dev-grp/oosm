package com.xdev.ooms.sharedkernel.communicator.models.shared;

import com.xdev.ooms.sharedkernel.Enum.QualityGrades;
import com.xdev.ooms.sharedkernel.Enum.StorageStatus;
import com.xdev.ooms.sharedkernel.basetype.dto.BaseTypeDto;
import com.xdev.ooms.sharedkernel.communicator.models.common.dtos.BaseDto;

import java.time.LocalDateTime;

public class StorageUnitDto extends BaseDto {

    private String name;
    private String location;
    private String description;

    private Double maxCapacity = 0.0;
    private Double currentVolume = 0.0;

    private LocalDateTime nextMaintenanceDate;
    private LocalDateTime lastInspectionDate;
    private boolean paidStorage;
    private double monthlyRentalPrice;
    private Double avgCost = 0.0;
    private Double totalCost = 0.0;
    private BaseTypeDto oilType;
    private StorageStatus status = StorageStatus.AVAILABLE;
    private LocalDateTime lastFillDate;
    private LocalDateTime lastEmptyDate;
    private SupplierDto supplier;
    private QualityGrades qualityGrade;
    private String lotNumber;
    private Boolean filteredOil;


    public StorageUnitDto() {
    }

    public boolean isPaidStorage() {
        return paidStorage;
    }

    public void setPaidStorage(boolean paidStorage) {
        this.paidStorage = paidStorage;
    }

    public double getMonthlyRentalPrice() {
        return monthlyRentalPrice;
    }

    public void setMonthlyRentalPrice(double monthlyRentalPrice) {
        this.monthlyRentalPrice = monthlyRentalPrice;
    }

    public SupplierDto getSupplier() {
        return supplier;
    }

    public void setSupplier(SupplierDto supplier) {
        this.supplier = supplier;
    }

    public double getFillPercentage() {
        return maxCapacity != null && maxCapacity > 0 ? (currentVolume / maxCapacity) * 100.0 : 0.0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Double maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public Double getCurrentVolume() {
        return currentVolume;
    }

    public void setCurrentVolume(Double currentVolume) {
        this.currentVolume = currentVolume;
    }

    public LocalDateTime getNextMaintenanceDate() {
        return nextMaintenanceDate;
    }

    public void setNextMaintenanceDate(LocalDateTime nextMaintenanceDate) {
        this.nextMaintenanceDate = nextMaintenanceDate;
    }

    public LocalDateTime getLastInspectionDate() {
        return lastInspectionDate;
    }

    public void setLastInspectionDate(LocalDateTime lastInspectionDate) {
        this.lastInspectionDate = lastInspectionDate;
    }

    public BaseTypeDto getOilType() {
        return oilType;
    }

    public void setOilType(BaseTypeDto oilType) {
        this.oilType = oilType;
    }

    public StorageStatus getStatus() {
        return status;
    }

    public void setStatus(StorageStatus status) {
        this.status = status;
    }

    public LocalDateTime getLastFillDate() {
        return lastFillDate;
    }

    public void setLastFillDate(LocalDateTime lastFillDate) {
        this.lastFillDate = lastFillDate;
    }

    public LocalDateTime getLastEmptyDate() {
        return lastEmptyDate;
    }

    public void setLastEmptyDate(LocalDateTime lastEmptyDate) {
        this.lastEmptyDate = lastEmptyDate;
    }

    public Double getAvgCost() {
        return avgCost;
    }

    public void setAvgCost(Double avgCost) {
        this.avgCost = avgCost;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }

    public QualityGrades getQualityGrade() {
        return qualityGrade;
    }

    public void setQualityGrade(QualityGrades qualityGrade) {
        this.qualityGrade = qualityGrade;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public Boolean getFilteredOil() {
        return filteredOil;
    }

    public void setFilteredOil(Boolean filteredOil) {
        this.filteredOil = filteredOil;
    }
}
