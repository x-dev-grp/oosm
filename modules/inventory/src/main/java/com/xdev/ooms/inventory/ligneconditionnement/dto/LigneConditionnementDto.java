package com.xdev.ooms.inventory.ligneconditionnement.dto;

import com.xdev.ooms.inventory.Enum.Statue;
import com.xdev.ooms.inventory.ligneconditionnement.entity.LigneConditionnement;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LigneConditionnementDto extends BaseDto<LigneConditionnement> implements Serializable {
    private String code;
    private String nom;
    private String description;
    private Statue etat;
    private Integer vitesseNominale;
    private Integer tempsPreparation;
    private Integer tempsNettoyage;
    private String responsable;
    private Date dateDerniereMaintenance;
    private Date dateProchaineMaintenance;
    private String notes;
    private  boolean actif;
}