package com.xdev.ooms.inventory.emplacementstock.entity;

import com.xdev.ooms.inventory.Enum.CategorieArticle;
import com.xdev.ooms.inventory.Enum.TypeEmplacement;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
@Entity
public class EmplacementStock extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String code;

    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_emplacement", nullable = false)
    private TypeEmplacement typeEmplacement;

    @Column(name = "capacite_maximale")
    private String capaciteMaximale;

    @Column(name = "capacite_actuelle")
    private String capaciteActuelle ;

    @Column(name = "zone")
    private String zone;


    private Boolean disponible = true;

    @Column(name = "reserve_pour")
    private String reservePour;

    @Column(name = "conditions_speciales")
    private String conditionsSpeciales;

    @Column(name = "temperature_min")
    private Double temperatureMin;

    @Column(name = "temperature_max")
    private Double temperatureMax;

    private String description;

    @Column(length = 1000)
    private String notes;
    private Boolean actif = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "categorie_article_stocke")
    private CategorieArticle categorieArticleStocke;


    public EmplacementStock() {
    }

    public EmplacementStock(String code, String nom, TypeEmplacement typeEmplacement, String capaciteMaximale, String capaciteActuelle, String zone, Boolean disponible, String reservePour, String conditionsSpeciales, Double temperatureMin, Double temperatureMax, String description, String notes, Boolean actif, CategorieArticle categorieArticleStocke) {
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