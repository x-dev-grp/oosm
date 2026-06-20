package com.xdev.ooms.inventory.emplacementstock.dto;

import com.xdev.ooms.inventory.Enum.CategorieArticle;
import com.xdev.ooms.inventory.Enum.TypeEmplacement;
import com.xdev.ooms.inventory.emplacementstock.entity.EmplacementStock;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import java.io.Serializable;


public class EmplacementStockDto extends BaseDto<EmplacementStock> implements Serializable {
    private String code;
    private String nom;
    private TypeEmplacement typeEmplacement;
    private String capaciteMaximale;
    private String capaciteActuelle;
    private String zone;
    private Boolean disponible;
    private String reservePour;
    private String conditionsSpeciales;
    private Double temperatureMin;
    private Double temperatureMax;
    private String description;
    private String notes;
    private Boolean actif = true;
    private CategorieArticle categorieArticleStocke;

    public EmplacementStockDto() {
    }

    public EmplacementStockDto(String code, String nom, TypeEmplacement typeEmplacement, String capaciteMaximale, String capaciteActuelle, String zone, Boolean disponible, String reservePour, String conditionsSpeciales, Double temperatureMin, Double temperatureMax, String description, String notes, Boolean actif, CategorieArticle categorieArticleStocke) {
        this.code = code;
        this.nom = nom;
        this.typeEmplacement = typeEmplacement;
        this.capaciteMaximale = capaciteMaximale;
        this.capaciteActuelle = capaciteActuelle;
        this.zone = zone;
        this.disponible = disponible;
        this.reservePour = reservePour;
        this.conditionsSpeciales = conditionsSpeciales;
        this.temperatureMin = temperatureMin;
        this.temperatureMax = temperatureMax;
        this.description = description;
        this.notes = notes;
        this.actif = actif;
        this.categorieArticleStocke = categorieArticleStocke;
    }

    public String getCode() {
        return code;
    }

    public String getNom() {
        return nom;
    }

    public TypeEmplacement getTypeEmplacement() {
        return typeEmplacement;
    }

    public String getCapaciteMaximale() {
        return capaciteMaximale;
    }

    public String getCapaciteActuelle() {
        return capaciteActuelle;
    }

    public String getZone() {
        return zone;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public String getReservePour() {
        return reservePour;
    }

    public String getConditionsSpeciales() {
        return conditionsSpeciales;
    }

    public Double getTemperatureMin() {
        return temperatureMin;
    }

    public Double getTemperatureMax() {
        return temperatureMax;
    }

    public String getDescription() {
        return description;
    }

    public String getNotes() {
        return notes;
    }

    public Boolean getActif() {
        return actif;
    }

    public CategorieArticle getCategorieArticleStocke() {
        return categorieArticleStocke;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setTypeEmplacement(TypeEmplacement typeEmplacement) {
        this.typeEmplacement = typeEmplacement;
    }

    public void setCapaciteMaximale(String capaciteMaximale) {
        this.capaciteMaximale = capaciteMaximale;
    }

    public void setCapaciteActuelle(String capaciteActuelle) {
        this.capaciteActuelle = capaciteActuelle;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }

    public void setReservePour(String reservePour) {
        this.reservePour = reservePour;
    }

    public void setConditionsSpeciales(String conditionsSpeciales) {
        this.conditionsSpeciales = conditionsSpeciales;
    }

    public void setTemperatureMin(Double temperatureMin) {
        this.temperatureMin = temperatureMin;
    }

    public void setTemperatureMax(Double temperatureMax) {
        this.temperatureMax = temperatureMax;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public void setCategorieArticleStocke(CategorieArticle categorieArticleStocke) {
        this.categorieArticleStocke = categorieArticleStocke;
    }
}