package com.xdev.ooms.conditioning.projet.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xdev.ooms.conditioning.projet.entity.Projet;
import com.xdev.ooms.conditioning.projet.enums.TypeEmballage;
import com.xdev.ooms.conditioning.projet.enums.TypeProduit;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ProjetDto extends BaseDto<Projet> {

    private String code;

    private ClientDto client;

    @JsonAlias({"client_id", "projetClientId"})
    private UUID clientId;

    private TypeProduit typeProduit;

    private TypeEmballage typeEmballage;

    private Double quantiteCible;

    private String unite;

    private LocalDate dateLimiteLivraison;

    private BigDecimal prixUnitaire;

    private BigDecimal valeurTotale;

    private String conditionsLivraison;

    private String statut;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;

    private String publicCode;
    private String qrUrl;
    private String qrImageBase64;
    private Double tauxAvancement;
    private Double quantiteProduite;
    private Integer nombreOF;

    private List<ProjetProduitDto> produits;
    private List<ProjetReservationDto> reservations;
    private List<UUID> ligneIds;

    @JsonProperty("clientId")
    public UUID getClientId() {
        if (clientId != null) {
            return clientId;
        }

        return client != null ? client.getId() : null;
    }

    public void setClient(ClientDto client) {
        this.client = client;
        if (client != null && client.getId() != null) {
            this.clientId = client.getId();
        }
    }

    public String getCode() {
        return code;
    }

    public ClientDto getClient() {
        return client;
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

    public String getPublicCode() {
        return publicCode;
    }

    public String getQrUrl() {
        return qrUrl;
    }

    public String getQrImageBase64() {
        return qrImageBase64;
    }

    public Double getTauxAvancement() {
        return tauxAvancement;
    }

    public Double getQuantiteProduite() {
        return quantiteProduite;
    }

    public Integer getNombreOF() {
        return nombreOF;
    }

    public List<ProjetProduitDto> getProduits() {
        return produits;
    }

    public List<ProjetReservationDto> getReservations() {
        return reservations;
    }

    public List<UUID> getLigneIds() {
        return ligneIds;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
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

    public void setPublicCode(String publicCode) {
        this.publicCode = publicCode;
    }

    public void setQrUrl(String qrUrl) {
        this.qrUrl = qrUrl;
    }

    public void setQrImageBase64(String qrImageBase64) {
        this.qrImageBase64 = qrImageBase64;
    }

    public void setTauxAvancement(Double tauxAvancement) {
        this.tauxAvancement = tauxAvancement;
    }

    public void setQuantiteProduite(Double quantiteProduite) {
        this.quantiteProduite = quantiteProduite;
    }

    public void setNombreOF(Integer nombreOF) {
        this.nombreOF = nombreOF;
    }

    public void setProduits(List<ProjetProduitDto> produits) {
        this.produits = produits;
    }

    public void setReservations(List<ProjetReservationDto> reservations) {
        this.reservations = reservations;
    }

    public void setLigneIds(List<UUID> ligneIds) {
        this.ligneIds = ligneIds;
    }
}
