package com.xdev.ooms.conditioning.inventoryusage.dto;

import com.xdev.ooms.sharedkernel.communicator.models.common.dtos.BaseDto;
import com.xdev.ooms.sharedkernel.models.UniteMesure;
import java.util.Map;
import java.util.UUID;
public class ArticleSecDto extends BaseDto {
    private UUID id;
    private String nom;
    private UniteMesure um;
    private String categorie;
    private Map<String, Object> configuration;

    public UUID getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public UniteMesure getUm() {
        return um;
    }

    public String getCategorie() {
        return categorie;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setUm(UniteMesure um) {
        this.um = um;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }
}
