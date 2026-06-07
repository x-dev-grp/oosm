package com.xdev.ooms.inventory.config;

import lombok.Data;

@Data
public class ConsommableConfig implements ArticleConfig {
    private String sousType;
    private String usage;
    private String unit;
    private Double quantity;
    private Integer temperatureStockageCelsius;
}
