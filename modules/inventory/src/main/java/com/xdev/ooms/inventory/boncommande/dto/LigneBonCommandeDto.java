package com.xdev.ooms.inventory.boncommande.dto;

import com.xdev.ooms.inventory.articlesec.dto.ArticleSecDto;
import com.xdev.ooms.inventory.boncommande.entity.LigneBonCommande;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


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




    public UUID getBonCommandeId() {
        return bonCommandeId;
    }

    public UUID getArticleId() {
        return articleId;
    }

    public void setBonCommandeId(UUID bonCommandeId) {
        this.bonCommandeId = bonCommandeId;
    }

    public void setArticleId(UUID articleId) {
        this.articleId = articleId;
    }
}