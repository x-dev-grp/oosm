package com.xdev.ooms.conditioning.qualitycontrol.dto;


import com.xdev.ooms.conditioning.Enum.ResultStatus;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import com.xdev.ooms.conditioning.qualitycontrol.entity.QCResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class QCResultDTO extends BaseDto<QCResult> {
    private UUID controlPointId;
    private UUID ofId;
    private String valeur;
    private ResultStatus statut;
    private String commentaire;
    private String photo;
    private String signature;
    private LocalDateTime dateControle;
}