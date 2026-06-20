package com.xdev.ooms.inventory.config;

import java.util.UUID;

public class PaletteConfig implements ArticleConfig {
    private String type;            // EURO, AMERICAINE
    private String material;        // bois, plastique
    private int colisPerLayer;
    private int numberOfLayers;
    private int maxHeightCm;
    private boolean clientSpecific;
    private UUID colisId;

    public String getType() {
        return type;
    }

    public String getMaterial() {
        return material;
    }

    public int getColisPerLayer() {
        return colisPerLayer;
    }

    public int getNumberOfLayers() {
        return numberOfLayers;
    }

    public int getMaxHeightCm() {
        return maxHeightCm;
    }

    public boolean isClientSpecific() {
        return clientSpecific;
    }

    public UUID getColisId() {
        return colisId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public void setColisPerLayer(int colisPerLayer) {
        this.colisPerLayer = colisPerLayer;
    }

    public void setNumberOfLayers(int numberOfLayers) {
        this.numberOfLayers = numberOfLayers;
    }

    public void setMaxHeightCm(int maxHeightCm) {
        this.maxHeightCm = maxHeightCm;
    }

    public void setClientSpecific(boolean clientSpecific) {
        this.clientSpecific = clientSpecific;
    }

    public void setColisId(UUID colisId) {
        this.colisId = colisId;
    }
}