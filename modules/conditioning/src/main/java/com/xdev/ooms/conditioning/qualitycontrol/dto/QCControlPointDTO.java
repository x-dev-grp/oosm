package com.xdev.ooms.conditioning.qualitycontrol.dto;


import com.xdev.ooms.conditioning.Enum.ControlType;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import com.xdev.ooms.conditioning.qualitycontrol.entity.QCControlPoint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Data
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class QCControlPointDTO extends BaseDto<QCControlPoint> {
    private UUID planId;
    private String nom;
    private ControlType type;
    private Double minValue;
    private Double maxValue;
    private boolean blocking;
}