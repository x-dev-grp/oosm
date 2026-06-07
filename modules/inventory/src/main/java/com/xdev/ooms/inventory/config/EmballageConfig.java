package com.xdev.ooms.inventory.config;

import lombok.Data;

@Data
public class EmballageConfig implements ArticleConfig {
    private String sousType;
    private String material;
    private Dimensions dimensions;
    private Boolean clientBranding;
    private Double poidsGrammes;
}
