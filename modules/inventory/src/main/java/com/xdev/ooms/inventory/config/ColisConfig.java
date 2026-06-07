package com.xdev.ooms.inventory.config;

import lombok.Data;

import java.util.UUID;

@Data
public class ColisConfig implements ArticleConfig {
    private UUID unitArticleId;
    private int unitsPerColis;
    private Dimensions dimensions;
    private double maxWeightKg;
}