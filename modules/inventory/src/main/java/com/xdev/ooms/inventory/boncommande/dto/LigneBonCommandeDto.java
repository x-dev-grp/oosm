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
    ArticleSecDto article;
    Integer quantiteCommandee;
    Integer quantiteRecue;
    BigDecimal prixUnitaire;
    String remarque;
    private UUID bonCommandeId;
    private UUID articleId;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public UUID getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public Boolean getDeleted() {
        return isDeleted;
    }

    @Override
    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    @Override
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    @Override
    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @Override
    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    @Override
    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public ArticleSecDto getArticle() {
        return article;
    }

    public void setArticle(ArticleSecDto article) {
        this.article = article;
    }

    public Integer getQuantiteCommandee() {
        return quantiteCommandee;
    }

    public void setQuantiteCommandee(Integer quantiteCommandee) {
        this.quantiteCommandee = quantiteCommandee;
    }

    public Integer getQuantiteRecue() {
        return quantiteRecue;
    }

    public void setQuantiteRecue(Integer quantiteRecue) {
        this.quantiteRecue = quantiteRecue;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public String getRemarque() {
        return remarque;
    }

    public void setRemarque(String remarque) {
        this.remarque = remarque;
    }

    public UUID getBonCommandeId() {
        return bonCommandeId;
    }

    public void setBonCommandeId(UUID bonCommandeId) {
        this.bonCommandeId = bonCommandeId;
    }

    public UUID getArticleId() {
        return articleId;
    }

    public void setArticleId(UUID articleId) {
        this.articleId = articleId;
    }
}