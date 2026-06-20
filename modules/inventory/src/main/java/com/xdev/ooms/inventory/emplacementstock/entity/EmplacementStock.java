package com.xdev.ooms.inventory.emplacementstock.entity;

import com.xdev.ooms.inventory.Enum.CategorieArticle;
import com.xdev.ooms.inventory.Enum.TypeEmplacement;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmplacementStock extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String code;

    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_emplacement", nullable = false)
    private TypeEmplacement typeEmplacement;

    @Column(name = "capacite_maximale")
    private String capaciteMaximale;

    @Column(name = "capacite_actuelle")
    private String capaciteActuelle ;

    @Column(name = "zone")
    private String zone;


    private Boolean disponible = true;

    @Column(name = "reserve_pour")
    private String reservePour;

    @Column(name = "conditions_speciales")
    private String conditionsSpeciales;

    @Column(name = "temperature_min")
    private Double temperatureMin;

    @Column(name = "temperature_max")
    private Double temperatureMax;

    private String description;

    @Column(length = 1000)
    private String notes;
    private Boolean actif = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "categorie_article_stocke")
    private CategorieArticle categorieArticleStocke;

}