package com.xdev.ooms.inventory.articlesec.dto;

import java.util.UUID;

public class ArticleStockSummaryDto {
    private UUID articleId;
    private Integer quantiteActuelle;
    private Integer quantiteReservee;
    private Integer quantiteDisponible;
    private Boolean belowMinimum;

    public UUID getArticleId() {
        return articleId;
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

    public Boolean getBelowMinimum() {
        return belowMinimum;
    }

    public void setArticleId(UUID articleId) {
        this.articleId = articleId;
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

    public void setBelowMinimum(Boolean belowMinimum) {
        this.belowMinimum = belowMinimum;
    }
}
