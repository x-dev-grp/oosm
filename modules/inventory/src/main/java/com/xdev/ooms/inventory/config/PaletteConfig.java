package com.xdev.ooms.inventory.config;

import lombok.Data;

import java.util.UUID;

@Data
public class PaletteConfig implements ArticleConfig {
    private String type;            // EURO, AMERICAINE
    private String material;        // bois, plastique
    private int colisPerLayer;
    private int numberOfLayers;
    private int maxHeightCm;
    private boolean clientSpecific;
    private UUID colisId;
}