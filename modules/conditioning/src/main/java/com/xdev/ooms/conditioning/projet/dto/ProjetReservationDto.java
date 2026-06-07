package com.xdev.ooms.conditioning.projet.dto;

import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProjetReservationDto extends BaseDto<com.xdev.ooms.conditioning.projet.entity.ProjetReservation> {
    private UUID projetId;
    private UUID articleId;
    private Double quantiteReservee;
    private String statut;
}
