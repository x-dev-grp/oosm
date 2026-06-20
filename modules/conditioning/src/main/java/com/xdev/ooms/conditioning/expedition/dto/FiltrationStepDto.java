package com.xdev.ooms.conditioning.expedition.dto;

import java.util.Map;
import java.util.UUID;

public class FiltrationStepDto {
    private UUID operationId;
    private String sourceLotNumber;
    private String targetLotNumber;
    private Double volumeFiltered;
    private String timestamp;
    
    private UUID sourceStorageUnitId;
    private String sourceStorageUnitName;
    private Map<String, String> qualityControls;
    private java.util.List<IntakeStepDto> sourceIntakeChain = new java.util.ArrayList<>();

    public UUID getOperationId() {
        return operationId;
    }

    public String getSourceLotNumber() {
        return sourceLotNumber;
    }

    public String getTargetLotNumber() {
        return targetLotNumber;
    }

    public Double getVolumeFiltered() {
        return volumeFiltered;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public UUID getSourceStorageUnitId() {
        return sourceStorageUnitId;
    }

    public String getSourceStorageUnitName() {
        return sourceStorageUnitName;
    }

    public Map<String, String> getQualityControls() {
        return qualityControls;
    }

    public java.util.List<IntakeStepDto> getSourceIntakeChain() {
        return sourceIntakeChain;
    }

    public void setOperationId(UUID operationId) {
        this.operationId = operationId;
    }

    public void setSourceLotNumber(String sourceLotNumber) {
        this.sourceLotNumber = sourceLotNumber;
    }

    public void setTargetLotNumber(String targetLotNumber) {
        this.targetLotNumber = targetLotNumber;
    }

    public void setVolumeFiltered(Double volumeFiltered) {
        this.volumeFiltered = volumeFiltered;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setSourceStorageUnitId(UUID sourceStorageUnitId) {
        this.sourceStorageUnitId = sourceStorageUnitId;
    }

    public void setSourceStorageUnitName(String sourceStorageUnitName) {
        this.sourceStorageUnitName = sourceStorageUnitName;
    }

    public void setQualityControls(Map<String, String> qualityControls) {
        this.qualityControls = qualityControls;
    }

    public void setSourceIntakeChain(java.util.List<IntakeStepDto> sourceIntakeChain) {
        this.sourceIntakeChain = sourceIntakeChain;
    }
}
