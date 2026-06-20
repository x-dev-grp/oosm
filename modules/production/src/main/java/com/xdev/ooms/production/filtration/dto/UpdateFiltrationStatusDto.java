package com.xdev.ooms.production.filtration.dto;


import com.xdev.ooms.production.filtration.dto.FiltrationStatus;

import jakarta.validation.constraints.NotNull;
public class UpdateFiltrationStatusDto {

    @NotNull(message = "Le statut est requis")
    private FiltrationStatus status;  // Nouveau statut

    // [NOUVEAU] Ajout de la note optionnelle
    private String note;              // Note à ajouter lors du changement de statut

    public FiltrationStatus getStatus() {
        return status;
    }

    public String getNote() {
        return note;
    }

    public void setStatus(FiltrationStatus status) {
        this.status = status;
    }

    public void setNote(String note) {
        this.note = note;
    }
}