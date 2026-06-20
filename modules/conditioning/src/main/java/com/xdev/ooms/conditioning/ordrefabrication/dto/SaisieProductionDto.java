package com.xdev.ooms.conditioning.ordrefabrication.dto;



import java.math.BigDecimal;

public class SaisieProductionDto  {
    private BigDecimal quantiteBonne;
    private BigDecimal quantiteNC;
    private String motifNC;

    public BigDecimal getQuantiteBonne() {
        return quantiteBonne;
    }

    public BigDecimal getQuantiteNC() {
        return quantiteNC;
    }

    public String getMotifNC() {
        return motifNC;
    }

    public void setQuantiteBonne(BigDecimal quantiteBonne) {
        this.quantiteBonne = quantiteBonne;
    }

    public void setQuantiteNC(BigDecimal quantiteNC) {
        this.quantiteNC = quantiteNC;
    }

    public void setMotifNC(String motifNC) {
        this.motifNC = motifNC;
    }
}