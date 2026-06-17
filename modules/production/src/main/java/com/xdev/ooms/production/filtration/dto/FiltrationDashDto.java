package com.xdev.ooms.production.filtration.dto;

import com.xdev.ooms.production.filtration.entity.FiltrationOperation;
import com.xdev.ooms.production.storageunit.dto.StorageUnitDto;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
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
}
