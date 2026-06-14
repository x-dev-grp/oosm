package com.xdev.ooms.inventory.stocksec.dto;

import com.xdev.ooms.inventory.Enum.TypeMouvement;
import com.xdev.ooms.inventory.articlesec.dto.ArticleSecDto;
import com.xdev.ooms.inventory.stocksec.entity.MouvementStockSec;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@Data
public class MouvementStockSecDto  extends BaseDto<MouvementStockSec> implements Serializable{
    UUID id;
    UUID tenantId;
    Boolean isDeleted;
    String createdBy;
    LocalDateTime createdDate;
    String lastModifiedBy;
    LocalDateTime lastModifiedDate;
    UUID externalId;
    ArticleSecDto article;
    Integer quantite;
    TypeMouvement typeMouvement;
    String motif;
    LocalDateTime dateMouvement;

}