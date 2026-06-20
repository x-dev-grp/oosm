package com.xdev.ooms.inventory.materielsupplier.entity;

import com.xdev.ooms.inventory.Enum.MaterielSupplierCategory;
import com.xdev.ooms.sharedkernel.Enum.Currency;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
/**
 * Material / packaging supplier (inventory). Distinct from production {@code Supplier}
 * (olive farmers and oil sale clients under {@code /api/production/supplier}).
 */
@Entity

public class MaterielSupplier extends BaseEntity {

    private String code;
    private String nom;
    private String nomCommercial;
    private String email;
    private String telephone;
    private String fax;
    private String siteWeb;
    private String numeroTva;
    private String adresse;
    private String ville;
    private String codePostal;
    private String pays;
    private String contactNom;
    private String contactPrenom;
    private String contactEmail;
    private String contactTelephone;
    private Currency currency;
    private Integer delaiLivraisonMoyen;
    private String conditionsPaiement;
    private Boolean actif = true;
    private String certifications;
    private java.time.LocalDateTime dateDerniereCommande;

    @Enumerated(EnumType.STRING)
    @Column(name = "categorie_fournisseur")
    private MaterielSupplierCategory category;

    public String displayName() {
        if (nomCommercial != null && !nomCommercial.isBlank()) {
            return nomCommercial.trim();
        }
        return nom == null ? "" : nom.trim();
    }

    public MaterielSupplier() {
    }

    public MaterielSupplier(String code, String nom, String nomCommercial, String email, String telephone, String fax, String siteWeb, String numeroTva, String adresse, String ville, String codePostal, String pays, String contactNom, String contactPrenom, String contactEmail, String contactTelephone, Currency currency, Integer delaiLivraisonMoyen, String conditionsPaiement, Boolean actif, String certifications, java.time.LocalDateTime dateDerniereCommande, MaterielSupplierCategory category) {
        this.code = code;
        this.nom = nom;
        this.nomCommercial = nomCommercial;
        this.email = email;
        this.telephone = telephone;
        this.fax = fax;
        this.siteWeb = siteWeb;
        this.numeroTva = numeroTva;
        this.adresse = adresse;
        this.ville = ville;
        this.codePostal = codePostal;
        this.pays = pays;
        this.contactNom = contactNom;
        this.contactPrenom = contactPrenom;
        this.contactEmail = contactEmail;
        this.contactTelephone = contactTelephone;
        this.currency = currency;
        this.delaiLivraisonMoyen = delaiLivraisonMoyen;
        this.conditionsPaiement = conditionsPaiement;
        this.actif = actif;
        this.certifications = certifications;
        this.dateDerniereCommande = dateDerniereCommande;
        this.category = category;
    }

    public String getCode() {
        return code;
    }

    public String getNom() {
        return nom;
    }

    public String getNomCommercial() {
        return nomCommercial;
    }

    public String getEmail() {
        return email;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getFax() {
        return fax;
    }

    public String getSiteWeb() {
        return siteWeb;
    }

    public String getNumeroTva() {
        return numeroTva;
    }

    public String getAdresse() {
        return adresse;
    }

    public String getVille() {
        return ville;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public String getPays() {
        return pays;
    }

    public String getContactNom() {
        return contactNom;
    }

    public String getContactPrenom() {
        return contactPrenom;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getContactTelephone() {
        return contactTelephone;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Integer getDelaiLivraisonMoyen() {
        return delaiLivraisonMoyen;
    }

    public String getConditionsPaiement() {
        return conditionsPaiement;
    }

    public Boolean getActif() {
        return actif;
    }

    public String getCertifications() {
        return certifications;
    }

    public java.time.LocalDateTime getDateDerniereCommande() {
        return dateDerniereCommande;
    }

    public MaterielSupplierCategory getCategory() {
        return category;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setNomCommercial(String nomCommercial) {
        this.nomCommercial = nomCommercial;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public void setSiteWeb(String siteWeb) {
        this.siteWeb = siteWeb;
    }

    public void setNumeroTva(String numeroTva) {
        this.numeroTva = numeroTva;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public void setContactNom(String contactNom) {
        this.contactNom = contactNom;
    }

    public void setContactPrenom(String contactPrenom) {
        this.contactPrenom = contactPrenom;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public void setContactTelephone(String contactTelephone) {
        this.contactTelephone = contactTelephone;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setDelaiLivraisonMoyen(Integer delaiLivraisonMoyen) {
        this.delaiLivraisonMoyen = delaiLivraisonMoyen;
    }

    public void setConditionsPaiement(String conditionsPaiement) {
        this.conditionsPaiement = conditionsPaiement;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public void setCertifications(String certifications) {
        this.certifications = certifications;
    }

    public void setDateDerniereCommande(java.time.LocalDateTime dateDerniereCommande) {
        this.dateDerniereCommande = dateDerniereCommande;
    }

    public void setCategory(MaterielSupplierCategory category) {
        this.category = category;
    }
}
