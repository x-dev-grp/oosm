package com.xdev.ooms.inventory.config;

public class EmballageConfig implements ArticleConfig {
    private String sousType;
    private String material;
    private Dimensions dimensions;
    private Boolean clientBranding;
    private Double poidsGrammes;

    public String getSousType() {
        return sousType;
    }

    public String getMaterial() {
        return material;
    }

    public Dimensions getDimensions() {
        return dimensions;
    }

    public Boolean getClientBranding() {
        return clientBranding;
    }

    public Double getPoidsGrammes() {
        return poidsGrammes;
    }

    public void setSousType(String sousType) {
        this.sousType = sousType;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public void setDimensions(Dimensions dimensions) {
        this.dimensions = dimensions;
    }

    public void setClientBranding(Boolean clientBranding) {
        this.clientBranding = clientBranding;
    }

    public void setPoidsGrammes(Double poidsGrammes) {
        this.poidsGrammes = poidsGrammes;
    }
}
