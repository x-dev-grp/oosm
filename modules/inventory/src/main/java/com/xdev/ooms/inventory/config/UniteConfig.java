package com.xdev.ooms.inventory.config;

import lombok.Data;

@Data
public class UniteConfig implements ArticleConfig {
    private String material;
    private int volumeMl;
    private String color;
    private String neckType;
    private int weightGr;
}