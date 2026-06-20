package com.xdev.ooms.inventory.materielsupplier.dto;

import com.xdev.ooms.inventory.Enum.MaterielSupplierCategory;
import com.xdev.ooms.inventory.materielsupplier.entity.MaterielSupplier;
import com.xdev.ooms.sharedkernel.Enum.Currency;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import java.io.Serializable;
import java.time.LocalDateTime;

public class MaterielSupplierDto extends BaseDto<MaterielSupplier> implements Serializable {
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
    private MaterielSupplierCategory category;
    private Integer delaiLivraisonMoyen;
    private String conditionsPaiement;
    private Currency currency;
    private Boolean actif;
    private String certifications;
    private LocalDateTime dateDerniereCommande;

    public MaterielSupplierDto() {
    }

    public MaterielSupplierDto(String code, String nom, String nomCommercial, String email, String telephone, String fax, String siteWeb, String numeroTva, String adresse, String ville, String codePostal, String pays, String contactNom, String contactPrenom, String contactEmail, String contactTelephone, MaterielSupplierCategory category, Integer delaiLivraisonMoyen, String conditionsPaiement, Currency currency, Boolean actif, String certifications, LocalDateTime dateDerniereCommande) {
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
        this.category = category;
        this.delaiLivraisonMoyen = delaiLivraisonMoyen;
        this.conditionsPaiement = conditionsPaiement;
        this.currency = currency;
        this.actif = actif;
        this.certifications = certifications;
        this.dateDerniereCommande = dateDerniereCommande;
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

    public MaterielSupplierCategory getCategory() {
        return category;
    }

    public Integer getDelaiLivraisonMoyen() {
        return delaiLivraisonMoyen;
    }

    public String getConditionsPaiement() {
        return conditionsPaiement;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Boolean getActif() {
        return actif;
    }

    public String getCertifications() {
        return certifications;
    }

    public LocalDateTime getDateDerniereCommande() {
        return dateDerniereCommande;
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

    public void setCategory(MaterielSupplierCategory category) {
        this.category = category;
    }

    public void setDelaiLivraisonMoyen(Integer delaiLivraisonMoyen) {
        this.delaiLivraisonMoyen = delaiLivraisonMoyen;
    }

    public void setConditionsPaiement(String conditionsPaiement) {
        this.conditionsPaiement = conditionsPaiement;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public void setCertifications(String certifications) {
        this.certifications = certifications;
    }

    public void setDateDerniereCommande(LocalDateTime dateDerniereCommande) {
        this.dateDerniereCommande = dateDerniereCommande;
    }
}
