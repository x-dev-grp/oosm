package com.xdev.ooms.conditioning.ordrefabrication.entity;


import com.xdev.ooms.conditioning.Enum.QualityStatus;
import com.xdev.ooms.conditioning.Enum.StatutOF;
import com.xdev.ooms.conditioning.projet.entity.Projet;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
public class OrdreFabrication extends BaseEntity implements Serializable {

    @Column(unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutOF statut = StatutOF.PLANIFIE;

    private LocalDateTime dateDebutPrevue;
    private LocalDateTime dateFinPrevue;
    private LocalDateTime dateDebutReelle;
    private LocalDateTime dateFinReelle;

    @Column(nullable = false)
    private BigDecimal quantiteCible;

    private BigDecimal quantiteBonne = BigDecimal.ZERO;
    private BigDecimal quantiteNC = BigDecimal.ZERO;
    private Long dureeReelle;
    @Column(name = "sku_id", nullable = false)
    private UUID productId;
    @Column(name = "bom_id")
    private UUID bomId;
    @Column(name = "ligne_id")
    private UUID ligneId;

    @Column(name = "lot_vrac_id")
    private UUID lotVracId;

    @Column(name = "traceability_lot_id")
    private UUID traceabilityLotId;

    private String motifNC;

    @OneToMany(mappedBy = "of", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneOF> lignes = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private QualityStatus qualityStatus = QualityStatus.FREE;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id")
    private Projet projet;

    public UUID getSkuId() {
        return productId;
    }

    public void setSkuId(UUID skuId) {
        this.productId = skuId;
    }

    public String getCode() {
        return code;
    }

    public StatutOF getStatut() {
        return statut;
    }

    public LocalDateTime getDateDebutPrevue() {
        return dateDebutPrevue;
    }

    public LocalDateTime getDateFinPrevue() {
        return dateFinPrevue;
    }

    public LocalDateTime getDateDebutReelle() {
        return dateDebutReelle;
    }

    public LocalDateTime getDateFinReelle() {
        return dateFinReelle;
    }

    public BigDecimal getQuantiteCible() {
        return quantiteCible;
    }

    public BigDecimal getQuantiteBonne() {
        return quantiteBonne;
    }

    public BigDecimal getQuantiteNC() {
        return quantiteNC;
    }

    public Long getDureeReelle() {
        return dureeReelle;
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getBomId() {
        return bomId;
    }

    public UUID getLigneId() {
        return ligneId;
    }

    public UUID getLotVracId() {
        return lotVracId;
    }

    public UUID getTraceabilityLotId() {
        return traceabilityLotId;
    }

    public String getMotifNC() {
        return motifNC;
    }

    public List<LigneOF> getLignes() {
        return lignes;
    }

    public QualityStatus getQualityStatus() {
        return qualityStatus;
    }

    public Projet getProjet() {
        return projet;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setStatut(StatutOF statut) {
        this.statut = statut;
    }

    public void setDateDebutPrevue(LocalDateTime dateDebutPrevue) {
        this.dateDebutPrevue = dateDebutPrevue;
    }

    public void setDateFinPrevue(LocalDateTime dateFinPrevue) {
        this.dateFinPrevue = dateFinPrevue;
    }

    public void setDateDebutReelle(LocalDateTime dateDebutReelle) {
        this.dateDebutReelle = dateDebutReelle;
    }

    public void setDateFinReelle(LocalDateTime dateFinReelle) {
        this.dateFinReelle = dateFinReelle;
    }

    public void setQuantiteCible(BigDecimal quantiteCible) {
        this.quantiteCible = quantiteCible;
    }

    public void setQuantiteBonne(BigDecimal quantiteBonne) {
        this.quantiteBonne = quantiteBonne;
    }

    public void setQuantiteNC(BigDecimal quantiteNC) {
        this.quantiteNC = quantiteNC;
    }

    public void setDureeReelle(Long dureeReelle) {
        this.dureeReelle = dureeReelle;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public void setBomId(UUID bomId) {
        this.bomId = bomId;
    }

    public void setLigneId(UUID ligneId) {
        this.ligneId = ligneId;
    }

    public void setLotVracId(UUID lotVracId) {
        this.lotVracId = lotVracId;
    }

    public void setTraceabilityLotId(UUID traceabilityLotId) {
        this.traceabilityLotId = traceabilityLotId;
    }

    public void setMotifNC(String motifNC) {
        this.motifNC = motifNC;
    }

    public void setLignes(List<LigneOF> lignes) {
        this.lignes = lignes;
    }

    public void setQualityStatus(QualityStatus qualityStatus) {
        this.qualityStatus = qualityStatus;
    }

    public void setProjet(Projet projet) {
        this.projet = projet;
    }
}
