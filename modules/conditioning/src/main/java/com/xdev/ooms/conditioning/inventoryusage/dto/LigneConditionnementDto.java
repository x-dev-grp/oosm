package com.xdev.ooms.conditioning.inventoryusage.dto;

import java.util.UUID;
public class LigneConditionnementDto {
    private UUID id;
    private String code;
    private String nom;

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getNom() {
        return nom;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
}
