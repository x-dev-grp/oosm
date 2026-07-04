package com.xdev.ooms.production.filtration.dto;

import com.xdev.ooms.production.storageunit.entity.StorageUnit;

import com.xdev.ooms.production.filtration.dto.FiltrationStatus;
import com.xdev.ooms.production.storageunit.dto.StorageUnitDto;

import com.xdev.ooms.production.filtration.entity.FiltrationOperation;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link FiltrationOperation}
 */
public class FiltrationOperationDto implements Serializable {
    UUID id;
    UUID tenantId;
    Boolean isDeleted;
    String createdBy;
    LocalDateTime createdDate;
    String lastModifiedBy;
    LocalDateTime lastModifiedDate;
    String qrHex;
    String qrImageBase64;
    StorageUnitDto sourceStorageUnit;
    FiltrationStatus status;
    LocalDateTime operationDate;
    Double volumeToFilter;
    Double volumeAfter;
    Double lossVolume;
    Double lossPercent;
    String note;
    StorageUnitDto targetStorageUnit;
    String sourceLotNumber;
    String targetLotNumber;
}