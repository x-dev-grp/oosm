package com.xdev.ooms.inventory.emplacementstock.dto;

import com.xdev.ooms.inventory.Enum.CategorieArticle;
import com.xdev.ooms.inventory.Enum.TypeEmplacement;
import com.xdev.ooms.inventory.emplacementstock.entity.EmplacementStock;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import lombok.*;

import java.io.Serializable;


@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EmplacementStockDto extends BaseDto<EmplacementStock> implements Serializable {
    private String code;
    private String nom;
    private TypeEmplacement typeEmplacement;
    private String capaciteMaximale;
    private String capaciteActuelle;
    private String zone;
    private Boolean disponible;
    private String reservePour;
    private String conditionsSpeciales;
    private Double temperatureMin;
    private Double temperatureMax;
    private String description;
    private String notes;
    private Boolean actif = true;
    private CategorieArticle categorieArticleStocke;
}