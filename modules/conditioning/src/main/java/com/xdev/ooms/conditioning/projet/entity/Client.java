package com.xdev.ooms.conditioning.projet.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import  com.xdev.ooms.sharedkernel.Enum.ClientType;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Client extends BaseEntity {

    @Column(nullable = false)
    private String nom;

    @Column( unique = true)
    private String codeClient;
    @Enumerated(EnumType.STRING)
    @Column( nullable = false)
    private ClientType type;
    private String email;

    private String telephone;

    private String adresse;

    private String ville;

    private String pays;

    private String codePostal;

    private Boolean privateLabel = false;

    private String siret;

    private String numeroTva;

    @Column(length = 500)
    private String notes;
    private Boolean actif = true;
}