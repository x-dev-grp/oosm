package com.xdev.ooms.production.filtration.entity;


import com.xdev.ooms.production.storageunit.entity.StorageUnit;

import com.xdev.ooms.production.filtration.dto.FiltrationStatus;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

import static org.apache.commons.math3.util.Precision.round;

@Entity
@Table(
        name = "filtration_operation",
        indexes = {
                @Index(name = "ix_filtration_source_unit", columnList = "source_storage_unit_id"),
                @Index(name = "ix_filtration_target_unit", columnList = "target_storage_unit_id")
        }
)
public class FiltrationOperation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_storage_unit_id", nullable = false)
    private StorageUnit sourceStorageUnit;

    // Getters and setters...
    @Enumerated(EnumType.STRING)  // Added this if FiltrationStatus is an enum
    private FiltrationStatus status;

    @Column(nullable = false)
    private LocalDateTime operationDate = LocalDateTime.now();

    private Double volumeToFilter = 0.0;
    private Double volumeAfter = 0.0;

    private Double lossVolume = 0.0;
    private Double lossPercent = 0.0;

    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_storage_unit_id", nullable = false)
    private StorageUnit targetStorageUnit;

    public void setVolumeToFilter(Double volumeBefore) { this.volumeToFilter = volumeBefore == null ? null : round(volumeBefore, 3); }

    public void setVolumeAfter(Double volumeAfter) { this.volumeAfter = volumeAfter == null ? null : round(volumeAfter, 3); }

    public void setLossVolume(Double lossVolume) { this.lossVolume = lossVolume == null ? null : round(lossVolume, 3); }

    public void setLossPercent(Double lossPercent) { this.lossPercent = lossPercent == null ? null : round(lossPercent, 3); }




    //pour une traçabilité fiable, même si la cuve change de lot plus tard
    @Column(name = "source_lot_number")
    private String sourceLotNumber;

    @Column(name = "target_lot_number")
    private String targetLotNumber;

// Getters / setters


    public StorageUnit getSourceStorageUnit() {
        return sourceStorageUnit;
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

    public Double getLossVolume() {
        return lossVolume;
    }

    public Double getLossPercent() {
        return lossPercent;
    }

    public String getNote() {
        return note;
    }

    public StorageUnit getTargetStorageUnit() {
        return targetStorageUnit;
    }

    public String getSourceLotNumber() {
        return sourceLotNumber;
    }

    public String getTargetLotNumber() {
        return targetLotNumber;
    }

    public void setSourceStorageUnit(StorageUnit sourceStorageUnit) {
        this.sourceStorageUnit = sourceStorageUnit;
    }

    public void setStatus(FiltrationStatus status) {
        this.status = status;
    }

    public void setOperationDate(LocalDateTime operationDate) {
        this.operationDate = operationDate;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setTargetStorageUnit(StorageUnit targetStorageUnit) {
        this.targetStorageUnit = targetStorageUnit;
    }

    public void setSourceLotNumber(String sourceLotNumber) {
        this.sourceLotNumber = sourceLotNumber;
    }

    public void setTargetLotNumber(String targetLotNumber) {
        this.targetLotNumber = targetLotNumber;
    }
}
