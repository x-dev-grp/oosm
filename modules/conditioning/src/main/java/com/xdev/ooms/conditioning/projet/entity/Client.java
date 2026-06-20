package com.xdev.ooms.conditioning.projet.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import  com.xdev.ooms.sharedkernel.Enum.ClientType;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Client extends BaseEntity {

    @Column(nullable = false)
    private String nom;

    @Column( unique = true)
    private String codeClient;
    @Enumerated(EnumType.STRING)
    @Column( nullable = false)
    private ClientType type;
    private String email;

    private String telephone;

    private String adresse;

    private String ville;

    private String pays;

    private String codePostal;

    private Boolean privateLabel = false;

    private String siret;

    private String numeroTva;

    @Column(length = 500)
    private String notes;
    private Boolean actif = true;

    public Client() {
    }

    public Client(String nom, String codeClient, ClientType type, String email, String telephone, String adresse, String ville, String pays, String codePostal, Boolean privateLabel, String siret, String numeroTva, String notes, Boolean actif) {
        this.nom = nom;
        this.codeClient = codeClient;
        this.type = type;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.ville = ville;
        this.pays = pays;
        this.codePostal = codePostal;
        this.privateLabel = privateLabel;
        this.siret = siret;
        this.numeroTva = numeroTva;
        this.notes = notes;
        this.actif = actif;
    }

    public String getNom() {
        return nom;
    }

    public String getCodeClient() {
        return codeClient;
    }

    public ClientType getType() {
        return type;
    }

    public String getEmail() {
        return email;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public String getVille() {
        return ville;
    }

    public String getPays() {
        return pays;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public Boolean getPrivateLabel() {
        return privateLabel;
    }

    public String getSiret() {
        return siret;
    }

    public String getNumeroTva() {
        return numeroTva;
    }

    public String getNotes() {
        return notes;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setCodeClient(String codeClient) {
        this.codeClient = codeClient;
    }

    public void setType(ClientType type) {
        this.type = type;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public void setPrivateLabel(Boolean privateLabel) {
        this.privateLabel = privateLabel;
    }

    public void setSiret(String siret) {
        this.siret = siret;
    }

    public void setNumeroTva(String numeroTva) {
        this.numeroTva = numeroTva;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }
}