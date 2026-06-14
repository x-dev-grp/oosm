package com.xdev.ooms.conditioning.qualitycontrol.dto;

import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import com.xdev.ooms.conditioning.qualitycontrol.entity.QCPlan;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Data
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class QCPlanDTO extends BaseDto<QCPlan> {
    private UUID ofId;
    private UUID id;
    private String titre;
    private boolean actif;
    private List<QCControlPointDTO> points;  // ou simplement List<UUID> pointIds
}