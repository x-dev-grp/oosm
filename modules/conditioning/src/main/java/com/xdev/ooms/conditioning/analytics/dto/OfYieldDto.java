package com.xdev.ooms.conditioning.analytics.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class OfYieldDto {
    private String ofCode;
    private String statut;
    private UUID skuId;
    private BigDecimal quantiteCible;
    private BigDecimal quantiteBonne;
    private BigDecimal yieldPercentage;

    public String getOfCode() {
        return ofCode;
    }

    public String getStatut() {
        return statut;
    }

    public UUID getSkuId() {
        return skuId;
    }

    public BigDecimal getQuantiteCible() {
        return quantiteCible;
    }

    public BigDecimal getQuantiteBonne() {
        return quantiteBonne;
    }

    public BigDecimal getYieldPercentage() {
        return yieldPercentage;
    }

    public void setOfCode(String ofCode) {
        this.ofCode = ofCode;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public void setSkuId(UUID skuId) {
        this.skuId = skuId;
    }

    public void setQuantiteCible(BigDecimal quantiteCible) {
        this.quantiteCible = quantiteCible;
    }

    public void setQuantiteBonne(BigDecimal quantiteBonne) {
        this.quantiteBonne = quantiteBonne;
    }

    public void setYieldPercentage(BigDecimal yieldPercentage) {
        this.yieldPercentage = yieldPercentage;
    }
}
