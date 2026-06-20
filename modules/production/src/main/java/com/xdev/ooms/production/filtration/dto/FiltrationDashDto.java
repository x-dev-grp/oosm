package com.xdev.ooms.production.filtration.dto;

import com.xdev.ooms.production.filtration.entity.FiltrationOperation;
import com.xdev.ooms.production.storageunit.dto.StorageUnitDto;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import java.time.LocalDateTime;

public class FiltrationDashDto extends BaseDto<FiltrationOperation> {
    private StorageUnitDto sourceStorageUnit;
    private StorageUnitDto targetStorageUnit;
    private FiltrationStatus status;
    private LocalDateTime operationDate;
    private Double volumeToFilter;
    private Double volumeAfter;
    private Double lossPercent;
    private String note;
    private String sourceLotNumber;
    private String targetLotNumber;

    public StorageUnitDto getSourceStorageUnit() {
        return sourceStorageUnit;
    }

    public StorageUnitDto getTargetStorageUnit() {
        return targetStorageUnit;
    }

    public FiltrationStatus getStatus() {
        return status;
    }

    public LocalDateTime getOperationDate() {
        return operationDate;
    }

    public Double getVolumeToFilter() {
        return volumeToFilter;
    }

    public Double getVolumeAfter() {
        return volumeAfter;
    }

    public Double getLossPercent() {
        return lossPercent;
    }

    public String getNote() {
        return note;
    }

    public String getSourceLotNumber() {
        return sourceLotNumber;
    }

    public String getTargetLotNumber() {
        return targetLotNumber;
    }

    public void setSourceStorageUnit(StorageUnitDto sourceStorageUnit) {
        this.sourceStorageUnit = sourceStorageUnit;
    }

    public void setTargetStorageUnit(StorageUnitDto targetStorageUnit) {
        this.targetStorageUnit = targetStorageUnit;
    }

    public void setStatus(FiltrationStatus status) {
        this.status = status;
    }

    public void setOperationDate(LocalDateTime operationDate) {
        this.operationDate = operationDate;
    }

    public void setVolumeToFilter(Double volumeToFilter) {
        this.volumeToFilter = volumeToFilter;
    }

    public void setVolumeAfter(Double volumeAfter) {
        this.volumeAfter = volumeAfter;
    }

    public void setLossPercent(Double lossPercent) {
        this.lossPercent = lossPercent;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setSourceLotNumber(String sourceLotNumber) {
        this.sourceLotNumber = sourceLotNumber;
    }

    public void setTargetLotNumber(String targetLotNumber) {
        this.targetLotNumber = targetLotNumber;
    }
}
