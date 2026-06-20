package com.xdev.ooms.conditioning.projet.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.xdev.ooms.conditioning.ordrefabrication.entity.OrdreFabrication;
import com.xdev.ooms.conditioning.projet.enums.TypeEmballage;
import com.xdev.ooms.conditioning.projet.enums.TypeProduit;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
public class Projet extends BaseEntity {

    @Column(unique = true, length = 100)
    private String code;

    @OneToMany(mappedBy = "projet", cascade = CascadeType.ALL)
    private List<OrdreFabrication> ordresFabrication = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Enumerated(EnumType.STRING)
    private TypeProduit typeProduit;

    @Enumerated(EnumType.STRING)
    private TypeEmballage typeEmballage;

    private Double quantiteCible;

    private String unite;

    private LocalDate dateLimiteLivraison;

    private BigDecimal prixUnitaire;

    private BigDecimal valeurTotale;

    @Column(length = 2000)
    private String conditionsLivraison;

    private String statut;

    @OneToMany(mappedBy = "projet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjetProduit> produits = new ArrayList<>();

    @OneToMany(mappedBy = "projet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjetReservation> reservations = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "projet_lignes_conditionnement", joinColumns = @JoinColumn(name = "projet_id"))
    @Column(name = "ligne_id", nullable = false)
    private List<UUID> ligneIds = new ArrayList<>();

    public String getCode() {
        return code;
    }

    public List<OrdreFabrication> getOrdresFabrication() {
        return ordresFabrication;
    }

    public Client getClient() {
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

    public List<ProjetProduit> getProduits() {
        return produits;
    }

    public List<ProjetReservation> getReservations() {
        return reservations;
    }

    public List<UUID> getLigneIds() {
        return ligneIds;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setOrdresFabrication(List<OrdreFabrication> ordresFabrication) {
        this.ordresFabrication = ordresFabrication;
    }

    public void setClient(Client client) {
        this.client = client;
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

    public void setProduits(List<ProjetProduit> produits) {
        this.produits = produits;
    }

    public void setReservations(List<ProjetReservation> reservations) {
        this.reservations = reservations;
    }

    public void setLigneIds(List<UUID> ligneIds) {
        this.ligneIds = ligneIds;
    }
}
