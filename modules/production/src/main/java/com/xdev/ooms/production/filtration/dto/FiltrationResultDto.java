package com.xdev.ooms.production.filtration.dto;

import com.xdev.ooms.production.storageunit.entity.StorageUnit;

import com.xdev.ooms.production.storageunit.dto.StorageUnitDto;

import java.time.LocalDateTime;
import java.util.UUID;

public class FiltrationResultDto {

    private UUID operationId;           // ID de l'opération
    private StorageUnitDto source;               // Unité source
    private StorageUnitDto target;               // Unité cible
    private double volumeFiltered;       // Volume initial à filtrer

    // [NOUVEAU] Champs ajoutés pour la complétion
    private Double volumeAfter;          // Volume après filtration
    private Double lossVolume;           // Volume perdu pendant la filtration
    private Double lossPercent;          // Pourcentage de perte

    private String status;                // Statut (CREATED, IN_PROGRESS, COMPLETED)
    private LocalDateTime timestamp;      // Date de l'opération
    private String note;



    //pour une traçabilité fiable, même si la cuve change de lot plus tard
    //pour les renvoyer au frontend.
    private String sourceLotNumber;
    private String targetLotNumber;// Note

    public UUID getOperationId() {
        return operationId;
    }

    public StorageUnitDto getSource() {
        return source;
    }

    public StorageUnitDto getTarget() {
        return target;
    }

    public double getVolumeFiltered() {
        return volumeFiltered;
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

    public String getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
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

    public void setOperationId(UUID operationId) {
        this.operationId = operationId;
    }

    public void setSource(StorageUnitDto source) {
        this.source = source;
    }

    public void setTarget(StorageUnitDto target) {
        this.target = target;
    }

    public void setVolumeFiltered(double volumeFiltered) {
        this.volumeFiltered = volumeFiltered;
    }

    public void setVolumeAfter(Double volumeAfter) {
        this.volumeAfter = volumeAfter;
    }

    public void setLossVolume(Double lossVolume) {
        this.lossVolume = lossVolume;
    }

    public void setLossPercent(Double lossPercent) {
        this.lossPercent = lossPercent;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
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