package com.xdev.ooms.inventory.articlesec.dto;

import java.util.UUID;

public class ArticleCritiqueDto {
    private UUID id;
    private String sku;
    private String nom;
    private Integer stockActuel;
    private Integer stockMinimum;
    private String categorie;
    private Integer stockDisponible;

    public ArticleCritiqueDto() {
    }

    public ArticleCritiqueDto(UUID id, String sku, String nom, Integer stockActuel, Integer stockMinimum, String categorie, Integer stockDisponible) {
        this.id = id;
        this.sku = sku;
        this.nom = nom;
        this.stockActuel = stockActuel;
        this.stockMinimum = stockMinimum;
        this.categorie = categorie;
        this.stockDisponible = stockDisponible;
    }

    public UUID getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getNom() {
        return nom;
    }

    public Integer getStockActuel() {
        return stockActuel;
    }

    public Integer getStockMinimum() {
        return stockMinimum;
    }

    public String getCategorie() {
        return categorie;
    }

    public Integer getStockDisponible() {
        return stockDisponible;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setStockActuel(Integer stockActuel) {
        this.stockActuel = stockActuel;
    }

    public void setStockMinimum(Integer stockMinimum) {
        this.stockMinimum = stockMinimum;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public void setStockDisponible(Integer stockDisponible) {
        this.stockDisponible = stockDisponible;
    }
}
