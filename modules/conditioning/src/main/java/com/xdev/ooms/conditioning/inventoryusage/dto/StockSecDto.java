package com.xdev.ooms.conditioning.inventoryusage.dto;


import java.util.UUID;

public class StockSecDto {
    private UUID id;
    private UUID articleId;
    private Integer quantiteActuelle;
    private Integer quantiteReservee;
    private Integer quantiteDisponible;

    public UUID getId() {
        return id;
    }

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

    public void setId(UUID id) {
        this.id = id;
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
}
