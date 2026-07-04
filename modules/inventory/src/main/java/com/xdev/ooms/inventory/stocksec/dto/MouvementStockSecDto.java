package com.xdev.ooms.inventory.stocksec.dto;

import com.xdev.ooms.inventory.Enum.TypeMouvement;
import com.xdev.ooms.inventory.articlesec.dto.ArticleSecDto;
import com.xdev.ooms.inventory.stocksec.entity.MouvementStockSec;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;


public class MouvementStockSecDto  extends BaseDto<MouvementStockSec> implements Serializable{
    UUID id;
    UUID tenantId;
    Boolean isDeleted;
    String createdBy;
    LocalDateTime createdDate;
    String lastModifiedBy;
    LocalDateTime lastModifiedDate;
    ArticleSecDto article;
    Integer quantite;
    TypeMouvement typeMouvement;
    String motif;
    LocalDateTime dateMouvement;

}