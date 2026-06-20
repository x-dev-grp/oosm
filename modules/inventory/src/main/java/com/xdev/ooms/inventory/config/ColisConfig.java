package com.xdev.ooms.inventory.config;

import java.util.UUID;

public class ColisConfig implements ArticleConfig {
    private UUID unitArticleId;
    private int unitsPerColis;
    private Dimensions dimensions;
    private double maxWeightKg;

    public UUID getUnitArticleId() {
        return unitArticleId;
    }

    public int getUnitsPerColis() {
        return unitsPerColis;
    }

    public Dimensions getDimensions() {
        return dimensions;
    }

    public double getMaxWeightKg() {
        return maxWeightKg;
    }

    public void setUnitArticleId(UUID unitArticleId) {
        this.unitArticleId = unitArticleId;
    }

    public void setUnitsPerColis(int unitsPerColis) {
        this.unitsPerColis = unitsPerColis;
    }

    public void setDimensions(Dimensions dimensions) {
        this.dimensions = dimensions;
    }

    public void setMaxWeightKg(double maxWeightKg) {
        this.maxWeightKg = maxWeightKg;
    }
}