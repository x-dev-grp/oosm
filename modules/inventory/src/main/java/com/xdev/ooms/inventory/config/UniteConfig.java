package com.xdev.ooms.inventory.config;

public class UniteConfig implements ArticleConfig {
    private String material;
    private int volumeMl;
    private String color;
    private String neckType;
    private int weightGr;

    public String getMaterial() {
        return material;
    }

    public int getVolumeMl() {
        return volumeMl;
    }

    public String getColor() {
        return color;
    }

    public String getNeckType() {
        return neckType;
    }

    public int getWeightGr() {
        return weightGr;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public void setVolumeMl(int volumeMl) {
        this.volumeMl = volumeMl;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setNeckType(String neckType) {
        this.neckType = neckType;
    }

    public void setWeightGr(int weightGr) {
        this.weightGr = weightGr;
    }
}