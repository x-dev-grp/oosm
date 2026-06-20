package com.xdev.ooms.inventory.config;

public class ConsommableConfig implements ArticleConfig {
    private String sousType;
    private String usage;
    private String unit;
    private Double quantity;
    private Integer temperatureStockageCelsius;

    public String getSousType() {
        return sousType;
    }

    public String getUsage() {
        return usage;
    }

    public String getUnit() {
        return unit;
    }

    public Double getQuantity() {
        return quantity;
    }

    public Integer getTemperatureStockageCelsius() {
        return temperatureStockageCelsius;
    }

    public void setSousType(String sousType) {
        this.sousType = sousType;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public void setTemperatureStockageCelsius(Integer temperatureStockageCelsius) {
        this.temperatureStockageCelsius = temperatureStockageCelsius;
    }
}
