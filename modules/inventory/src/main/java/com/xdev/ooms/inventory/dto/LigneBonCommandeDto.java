package com.xdev.ooms.inventory.dto;

import com.xdev.ooms.inventory.entity.LigneBonCommande;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@Data
public class LigneBonCommandeDto extends BaseDto<LigneBonCommande> implements Serializable {
    UUID id;
    UUID tenantId;
    Boolean isDeleted;
    String createdBy;
    LocalDateTime createdDate;
    String lastModifiedBy;
    LocalDateTime lastModifiedDate;
    UUID externalId;
    ArticleSecDto article;
    Integer quantiteCommandee;
    Integer quantiteRecue;
    BigDecimal prixUnitaire;
    String remarque;
    private UUID bonCommandeId;
    private UUID articleId;



}