package com.xdev.ooms.conditioning.projet.dto;

import com.xdev.ooms.conditioning.projet.entity.Projet;
import com.xdev.ooms.conditioning.projet.enums.TypeEmballage;
import com.xdev.ooms.conditioning.projet.enums.TypeProduit;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class ProjetResponseDto extends BaseDto<Projet> {
    private UUID id;
    private String clientNom;
    private String clientEmail;
    private TypeProduit typeProduit;
    private TypeEmballage typeEmballage;
    private Double quantiteCible;
    private String unite;
    private LocalDate dateLimiteLivraison;
    private BigDecimal prixUnitaire;
    private BigDecimal valeurTotale;
    private String conditionsLivraison;
    private String statut;
    private LocalDateTime createdDate;

    // QR code fields
    private String qrCode;
    private String qrImageBase64;

    public UUID getId() {
        return id;
    }

    public String getClientNom() {
        return clientNom;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public TypeProduit getTypeProduit() {
        return typeProduit;
    }

    public TypeEmballage getTypeEmballage() {
        return typeEmballage;
    }

    public Double getQuantiteCible() {
        return quantiteCible;
    }

    public String getUnite() {
        return unite;
    }

    public LocalDate getDateLimiteLivraison() {
        return dateLimiteLivraison;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public BigDecimal getValeurTotale() {
        return valeurTotale;
    }

    public String getConditionsLivraison() {
        return conditionsLivraison;
    }

    public String getStatut() {
        return statut;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public String getQrCode() {
        return qrCode;
    }

    public String getQrImageBase64() {
        return qrImageBase64;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setClientNom(String clientNom) {
        this.clientNom = clientNom;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public void setTypeProduit(TypeProduit typeProduit) {
        this.typeProduit = typeProduit;
    }

    public void setTypeEmballage(TypeEmballage typeEmballage) {
        this.typeEmballage = typeEmballage;
    }

    public void setQuantiteCible(Double quantiteCible) {
        this.quantiteCible = quantiteCible;
    }

    public void setUnite(String unite) {
        this.unite = unite;
    }

    public void setDateLimiteLivraison(LocalDate dateLimiteLivraison) {
        this.dateLimiteLivraison = dateLimiteLivraison;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public void setValeurTotale(BigDecimal valeurTotale) {
        this.valeurTotale = valeurTotale;
    }

    public void setConditionsLivraison(String conditionsLivraison) {
        this.conditionsLivraison = conditionsLivraison;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public void setQrImageBase64(String qrImageBase64) {
        this.qrImageBase64 = qrImageBase64;
    }
}