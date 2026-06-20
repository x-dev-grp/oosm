package com.xdev.ooms.conditioning.ordrefabrication.dto;


import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xdev.ooms.conditioning.Enum.QualityStatus;
import com.xdev.ooms.conditioning.Enum.StatutOF;
import com.xdev.ooms.conditioning.ordrefabrication.entity.OrdreFabrication;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class OrdreFabricationDto extends BaseDto<OrdreFabrication> {
    private UUID id;
    private String code;
    private StatutOF statut;
    private LocalDateTime dateDebutPrevue;
    private LocalDateTime dateFinPrevue;
    private LocalDateTime dateDebutReelle;
    private LocalDateTime dateFinReelle;
    private BigDecimal quantiteCible;
    private BigDecimal quantiteBonne;
    private BigDecimal quantiteNC;
    private Long dureeReelle;
    @JsonAlias("skuId")
    private UUID productId;
    private String productName;
    private UUID ligneId;
    private String ligneNom;
    private UUID lotVracId;
    private UUID traceabilityLotId;
    private List<LigneOFDto> lignes;
    private UUID bomId;
     private String motifNC;
    private String publicCode;
    private String qrUrl;
    private String qrImageBase64;
    private QualityStatus qualityStatus;
    private UUID projectId;
    private String projectCode;

    @JsonProperty("skuId")
    public UUID getSkuId() {
        return productId;
    }

    public void setSkuId(UUID skuId) {
        this.productId = skuId;
    }

    @JsonProperty("skuCode")
    public String getSkuCode() {
        return productName;
    }

    public void setSkuCode(String skuCode) {
        this.productName = skuCode;
    }

    public UUID getId() {
        return id;
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

    public String getProductName() {
        return productName;
    }

    public UUID getLigneId() {
        return ligneId;
    }

    public String getLigneNom() {
        return ligneNom;
    }

    public UUID getLotVracId() {
        return lotVracId;
    }

    public UUID getTraceabilityLotId() {
        return traceabilityLotId;
    }

    public List<LigneOFDto> getLignes() {
        return lignes;
    }

    public UUID getBomId() {
        return bomId;
    }

    public String getMotifNC() {
        return motifNC;
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

    public QualityStatus getQualityStatus() {
        return qualityStatus;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setLigneId(UUID ligneId) {
        this.ligneId = ligneId;
    }

    public void setLigneNom(String ligneNom) {
        this.ligneNom = ligneNom;
    }

    public void setLotVracId(UUID lotVracId) {
        this.lotVracId = lotVracId;
    }

    public void setTraceabilityLotId(UUID traceabilityLotId) {
        this.traceabilityLotId = traceabilityLotId;
    }

    public void setLignes(List<LigneOFDto> lignes) {
        this.lignes = lignes;
    }

    public void setBomId(UUID bomId) {
        this.bomId = bomId;
    }

    public void setMotifNC(String motifNC) {
        this.motifNC = motifNC;
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

    public void setQualityStatus(QualityStatus qualityStatus) {
        this.qualityStatus = qualityStatus;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }
}
