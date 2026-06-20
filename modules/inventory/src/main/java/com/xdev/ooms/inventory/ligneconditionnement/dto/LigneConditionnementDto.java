package com.xdev.ooms.inventory.ligneconditionnement.dto;

import com.xdev.ooms.inventory.Enum.Statue;
import com.xdev.ooms.inventory.ligneconditionnement.entity.LigneConditionnement;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import java.io.Serializable;
import java.util.Date;

public class LigneConditionnementDto extends BaseDto<LigneConditionnement> implements Serializable {
    private String code;
    private String nom;
    private String description;
    private Statue etat;
    private Integer vitesseNominale;
    private Integer tempsPreparation;
    private Integer tempsNettoyage;
    private String responsable;
    private Date dateDerniereMaintenance;
    private Date dateProchaineMaintenance;
    private String notes;
    private  boolean actif;

    public LigneConditionnementDto() {
    }

    public LigneConditionnementDto(String code, String nom, String description, Statue etat, Integer vitesseNominale, Integer tempsPreparation, Integer tempsNettoyage, String responsable, Date dateDerniereMaintenance, Date dateProchaineMaintenance, String notes, boolean actif) {
        this.code = code;
        this.nom = nom;
        this.description = description;
        this.etat = etat;
        this.vitesseNominale = vitesseNominale;
        this.tempsPreparation = tempsPreparation;
        this.tempsNettoyage = tempsNettoyage;
        this.responsable = responsable;
        this.dateDerniereMaintenance = dateDerniereMaintenance;
        this.dateProchaineMaintenance = dateProchaineMaintenance;
        this.notes = notes;
        this.actif = actif;
    }

    public String getCode() {
        return code;
    }

    public String getNom() {
        return nom;
    }

    public String getDescription() {
        return description;
    }

    public Statue getEtat() {
        return etat;
    }

    public Integer getVitesseNominale() {
        return vitesseNominale;
    }

    public Integer getTempsPreparation() {
        return tempsPreparation;
    }

    public Integer getTempsNettoyage() {
        return tempsNettoyage;
    }

    public String getResponsable() {
        return responsable;
    }

    public Date getDateDerniereMaintenance() {
        return dateDerniereMaintenance;
    }

    public Date getDateProchaineMaintenance() {
        return dateProchaineMaintenance;
    }

    public String getNotes() {
        return notes;
    }

    public boolean isActif() {
        return actif;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEtat(Statue etat) {
        this.etat = etat;
    }

    public void setVitesseNominale(Integer vitesseNominale) {
        this.vitesseNominale = vitesseNominale;
    }

    public void setTempsPreparation(Integer tempsPreparation) {
        this.tempsPreparation = tempsPreparation;
    }

    public void setTempsNettoyage(Integer tempsNettoyage) {
        this.tempsNettoyage = tempsNettoyage;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public void setDateDerniereMaintenance(Date dateDerniereMaintenance) {
        this.dateDerniereMaintenance = dateDerniereMaintenance;
    }

    public void setDateProchaineMaintenance(Date dateProchaineMaintenance) {
        this.dateProchaineMaintenance = dateProchaineMaintenance;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }
}