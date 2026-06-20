package com.xdev.ooms.production.genealogy.entity;


import com.xdev.ooms.production.genealogy.enums.TraceabilitySourceType;

import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class TraceabilityLot extends BaseEntity {

    @Column(name = "lot_number", nullable = false, length = 120)
    private String lotNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 40)
    private TraceabilitySourceType sourceType;

    @Column(name = "source_entity_id")
    private UUID sourceEntityId;

    @Column(name = "root_reception_id")
    private UUID rootReceptionId;

    @Column(name = "parent_lot_id")
    private UUID parentLotId;

    @Column(name = "storage_unit_id")
    private UUID storageUnitId;

    @Column(name = "filtration_operation_id")
    private UUID filtrationOperationId;

    @Column(name = "quality_grade")
    private String qualityGrade;

    @Column(name = "oil_variety")
    private String oilVariety;

    @Column(name = "quantity")
    private Double quantity;

    @Column(name = "captured_at", nullable = false)
    private LocalDateTime capturedAt;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "source_snapshot_json", columnDefinition = "TEXT")
    private String sourceSnapshotJson;

    public String getLotNumber() {
        return lotNumber;
    }

    public TraceabilitySourceType getSourceType() {
        return sourceType;
    }

    public UUID getSourceEntityId() {
        return sourceEntityId;
    }

    public UUID getRootReceptionId() {
        return rootReceptionId;
    }

    public UUID getParentLotId() {
        return parentLotId;
    }

    public UUID getStorageUnitId() {
        return storageUnitId;
    }

    public UUID getFiltrationOperationId() {
        return filtrationOperationId;
    }

    public String getQualityGrade() {
        return qualityGrade;
    }

    public String getOilVariety() {
        return oilVariety;
    }

    public Double getQuantity() {
        return quantity;
    }

    public LocalDateTime getCapturedAt() {
        return capturedAt;
    }

    public Boolean getActive() {
        return active;
    }

    public String getSourceSnapshotJson() {
        return sourceSnapshotJson;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public void setSourceType(TraceabilitySourceType sourceType) {
        this.sourceType = sourceType;
    }

    public void setSourceEntityId(UUID sourceEntityId) {
        this.sourceEntityId = sourceEntityId;
    }

    public void setRootReceptionId(UUID rootReceptionId) {
        this.rootReceptionId = rootReceptionId;
    }

    public void setParentLotId(UUID parentLotId) {
        this.parentLotId = parentLotId;
    }

    public void setStorageUnitId(UUID storageUnitId) {
        this.storageUnitId = storageUnitId;
    }

    public void setFiltrationOperationId(UUID filtrationOperationId) {
        this.filtrationOperationId = filtrationOperationId;
    }

    public void setQualityGrade(String qualityGrade) {
        this.qualityGrade = qualityGrade;
    }

    public void setOilVariety(String oilVariety) {
        this.oilVariety = oilVariety;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public void setCapturedAt(LocalDateTime capturedAt) {
        this.capturedAt = capturedAt;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setSourceSnapshotJson(String sourceSnapshotJson) {
        this.sourceSnapshotJson = sourceSnapshotJson;
    }
}
