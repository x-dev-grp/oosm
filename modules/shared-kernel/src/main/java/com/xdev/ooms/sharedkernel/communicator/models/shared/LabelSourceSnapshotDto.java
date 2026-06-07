package com.xdev.ooms.sharedkernel.communicator.models.shared;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.xdev.ooms.sharedkernel.communicator.models.common.dtos.BaseDto;
import com.xdev.ooms.sharedkernel.Enum.LabelSourceType;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LabelSourceSnapshotDto extends BaseDto {
    private LabelSourceType sourceType;
    private UUID sourceId;
    private String sourceBusinessKey;
    private String snapshotJson;


    public void setSourceType(LabelSourceType sourceType) {
        this.sourceType = sourceType;
    }


    public void setSourceId(UUID sourceId) {
        this.sourceId = sourceId;
    }


    public void setSourceBusinessKey(String sourceBusinessKey) {
        this.sourceBusinessKey = sourceBusinessKey;
    }


    public void setSnapshotJson(String snapshotJson) {
        this.snapshotJson = snapshotJson;
    }
}
