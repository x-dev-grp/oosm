package com.xdev.ooms.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleCritiqueDto {
    private UUID id;
    private String sku;
    private String nom;
    private Integer stockActuel;
    private Integer stockMinimum;
    private String categorie;
    private Integer stockDisponible;
}
