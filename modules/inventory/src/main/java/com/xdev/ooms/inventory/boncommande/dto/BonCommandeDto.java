package com.xdev.ooms.inventory.boncommande.dto;

import com.xdev.ooms.inventory.Enum.StatutBonCommande;
import com.xdev.ooms.inventory.boncommande.entity.BonCommande;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
 @Data
public class BonCommandeDto  extends BaseDto<BonCommande> implements Serializable{
    UUID id;
    UUID tenantId;
    Boolean isDeleted;
    String createdBy;
    LocalDateTime createdDate;
    String lastModifiedBy;
    LocalDateTime lastModifiedDate;
    UUID externalId;
    String numeroBC;
    LocalDateTime dateValidation;
    LocalDateTime dateReceptionPrevue;
    StatutBonCommande status;
    List<LigneBonCommandeDto> lignes;
    String motifRefus;

}