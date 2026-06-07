package com.xdev.ooms.inventory.entity;


import com.xdev.ooms.inventory.Enum.Statue;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.util.Date;

@Entity
@Data
@Audited
@NoArgsConstructor
@AllArgsConstructor
public class LigneConditionnement extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String nom;

    private String description;

    @Enumerated(EnumType.STRING)
    private Statue etat = Statue.ACTIF;

    private Integer vitesseNominale;

    private Integer tempsPreparation;

    private Integer tempsNettoyage;

    private String responsable;

    private Date dateDerniereMaintenance;

    private Date dateProchaineMaintenance;

    private String notes;
    private boolean actif;
}