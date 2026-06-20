package com.xdev.ooms.conditioning.inventoryusage.dto;

import java.util.UUID;

public class MaterialNeedLineDto {
    private UUID articleId;
    private String articleName;
    private String unitOfMeasure;
    private double quantityPerUnit;
    private double quantityNeeded;
    private int quantityNeededRounded;
    private Integer quantiteActuelle;
    private Integer quantiteReservee;
    private Integer quantiteDisponible;
    private boolean sufficient;

    public UUID getArticleId() {
        return articleId;
    }

    public String getArticleName() {
        return articleName;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public double getQuantityPerUnit() {
        return quantityPerUnit;
    }

    public double getQuantityNeeded() {
        return quantityNeeded;
    }

    public int getQuantityNeededRounded() {
        return quantityNeededRounded;
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

    public boolean isSufficient() {
        return sufficient;
    }

    public void setArticleId(UUID articleId) {
        this.articleId = articleId;
    }

    public void setArticleName(String articleName) {
        this.articleName = articleName;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public void setQuantityPerUnit(double quantityPerUnit) {
        this.quantityPerUnit = quantityPerUnit;
    }

    public void setQuantityNeeded(double quantityNeeded) {
        this.quantityNeeded = quantityNeeded;
    }

    public void setQuantityNeededRounded(int quantityNeededRounded) {
        this.quantityNeededRounded = quantityNeededRounded;
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

    public void setSufficient(boolean sufficient) {
        this.sufficient = sufficient;
    }
}
