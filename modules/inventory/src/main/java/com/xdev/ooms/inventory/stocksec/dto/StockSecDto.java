package com.xdev.ooms.inventory.stocksec.dto;

import com.xdev.ooms.inventory.articlesec.dto.ArticleSecDto;
import com.xdev.ooms.inventory.emplacementstock.dto.EmplacementStockDto;
import com.xdev.ooms.inventory.stocksec.entity.StockSec;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import java.io.Serializable;
import java.util.UUID;


public class StockSecDto extends BaseDto<StockSec> implements Serializable {

    private UUID articleId;
    private ArticleSecDto article;
    private Integer quantiteActuelle;
    private Integer quantiteReservee;
    private Integer quantiteDisponible;
    private UUID emplacementId;
    private EmplacementStockDto emplacement;

    public StockSecDto() {
    }

    public StockSecDto(UUID articleId, ArticleSecDto article, Integer quantiteActuelle, Integer quantiteReservee, Integer quantiteDisponible, UUID emplacementId, EmplacementStockDto emplacement) {
        this.articleId = articleId;
        this.article = article;
        this.quantiteActuelle = quantiteActuelle;
        this.quantiteReservee = quantiteReservee;
        this.quantiteDisponible = quantiteDisponible;
        this.emplacementId = emplacementId;
        this.emplacement = emplacement;
    }

    public UUID getArticleId() {
        return articleId;
    }

    public ArticleSecDto getArticle() {
        return article;
    }

    public Integer getQuantiteActuelle() {
        return quantiteActuelle;
    }

    public Integer getQuantiteReservee() {
        return quantiteReservee;
    }

    public Integer getQuantiteDisponible() {
        return quantiteDisponible;
    }

    public UUID getEmplacementId() {
        return emplacementId;
    }

    public EmplacementStockDto getEmplacement() {
        return emplacement;
    }

    public void setArticleId(UUID articleId) {
        this.articleId = articleId;
    }

    public void setArticle(ArticleSecDto article) {
        this.article = article;
    }

    public void setQuantiteActuelle(Integer quantiteActuelle) {
        this.quantiteActuelle = quantiteActuelle;
    }

    public void setQuantiteReservee(Integer quantiteReservee) {
        this.quantiteReservee = quantiteReservee;
    }

    public void setQuantiteDisponible(Integer quantiteDisponible) {
        this.quantiteDisponible = quantiteDisponible;
    }

    public void setEmplacementId(UUID emplacementId) {
        this.emplacementId = emplacementId;
    }

    public void setEmplacement(EmplacementStockDto emplacement) {
        this.emplacement = emplacement;
    }
}