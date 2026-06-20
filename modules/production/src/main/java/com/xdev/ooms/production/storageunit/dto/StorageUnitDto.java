package com.xdev.ooms.production.storageunit.dto;

import com.xdev.ooms.sharedkernel.basetype.entity.BaseType;

import com.xdev.ooms.sharedkernel.basetype.dto.BaseTypeDto;
import com.xdev.ooms.production.supplier.entity.Supplier;

import com.xdev.ooms.production.storageunit.entity.StorageUnit;
import com.xdev.ooms.sharedkernel.Enum.QualityGrades;
import com.xdev.ooms.sharedkernel.Enum.StorageStatus;
import com.xdev.ooms.production.supplier.dto.SupplierDto;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import java.time.LocalDateTime;

public class StorageUnitDto extends BaseDto<StorageUnit> {

    private String name;
    private String location;
    private String description;

    private Double maxCapacity = 0.0;
    private Double currentVolume = 0.0;

    private LocalDateTime nextMaintenanceDate;
    private LocalDateTime lastInspectionDate;

    private Double avgCost = 0.0;
    private Double totalCost = 0.0;

    private BaseTypeDto oilVariety; // OIL_VARIETY
    private StorageStatus status = StorageStatus.AVAILABLE;
    private Boolean paidStorage;
    private Boolean filteredOil;
    private Double monthlyRentalPrice;
    private QualityGrades qualityGrade;
    private String lotNumber;
    private LocalDateTime lastFillDate;
    private LocalDateTime lastEmptyDate;
    private SupplierDto supplier;
    private String qrHex;
    private String publicCode;
    private String qrUrl;
    private String qrImageBase64;

    public StorageUnitDto() {
    }

    // Optional helper for client-side rendering
    public double getFillPercentage() {
        return maxCapacity != null && maxCapacity > 0 ? (currentVolume / maxCapacity) * 100.0 : 0.0;
    }



    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public Double getMaxCapacity() {
        return maxCapacity;
    }

    public Double getCurrentVolume() {
        return currentVolume;
    }

    public LocalDateTime getNextMaintenanceDate() {
        return nextMaintenanceDate;
    }

    public LocalDateTime getLastInspectionDate() {
        return lastInspectionDate;
    }

    public Double getAvgCost() {
        return avgCost;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public BaseTypeDto getOilVariety() {
        return oilVariety;
    }

    public StorageStatus getStatus() {
        return status;
    }

    public Boolean getPaidStorage() {
        return paidStorage;
    }

    public Boolean getFilteredOil() {
        return filteredOil;
    }

    public Double getMonthlyRentalPrice() {
        return monthlyRentalPrice;
    }

    public QualityGrades getQualityGrade() {
        return qualityGrade;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public LocalDateTime getLastFillDate() {
        return lastFillDate;
    }

    public LocalDateTime getLastEmptyDate() {
        return lastEmptyDate;
    }

    public SupplierDto getSupplier() {
        return supplier;
    }

    public String getQrHex() {
        return qrHex;
    }

    public String getPublicCode() {
        return publicCode;
    }

    public String getQrUrl() {
        return qrUrl;
    }

    public String getQrImageBase64() {
        return qrImageBase64;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMaxCapacity(Double maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public void setCurrentVolume(Double currentVolume) {
        this.currentVolume = currentVolume;
    }

    public void setNextMaintenanceDate(LocalDateTime nextMaintenanceDate) {
        this.nextMaintenanceDate = nextMaintenanceDate;
    }

    public void setLastInspectionDate(LocalDateTime lastInspectionDate) {
        this.lastInspectionDate = lastInspectionDate;
    }

    public void setAvgCost(Double avgCost) {
        this.avgCost = avgCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }

    public void setOilVariety(BaseTypeDto oilVariety) {
        this.oilVariety = oilVariety;
    }

    public void setStatus(StorageStatus status) {
        this.status = status;
    }

    public void setPaidStorage(Boolean paidStorage) {
        this.paidStorage = paidStorage;
    }

    public void setFilteredOil(Boolean filteredOil) {
        this.filteredOil = filteredOil;
    }

    public void setMonthlyRentalPrice(Double monthlyRentalPrice) {
        this.monthlyRentalPrice = monthlyRentalPrice;
    }

    public void setQualityGrade(QualityGrades qualityGrade) {
        this.qualityGrade = qualityGrade;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public void setLastFillDate(LocalDateTime lastFillDate) {
        this.lastFillDate = lastFillDate;
    }

    public void setLastEmptyDate(LocalDateTime lastEmptyDate) {
        this.lastEmptyDate = lastEmptyDate;
    }

    public void setSupplier(SupplierDto supplier) {
        this.supplier = supplier;
    }

    public void setQrHex(String qrHex) {
        this.qrHex = qrHex;
    }

    public void setPublicCode(String publicCode) {
        this.publicCode = publicCode;
    }

    public void setQrUrl(String qrUrl) {
        this.qrUrl = qrUrl;
    }

    public void setQrImageBase64(String qrImageBase64) {
        this.qrImageBase64 = qrImageBase64;
    }
}
