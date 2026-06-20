package com.xdev.ooms.production.filtration.dto;



import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
/**
 * [NOUVEAU] DTO pour la complétion d'une opération de filtration
 * Ce DTO a été créé pour:
 * - Recevoir les données nécessaires quand on termine une opération
 * - Permettre à l'utilisateur de saisir le volume après filtration
 * - Ajouter une note de completion
 * Il est utilisé dans l'endpoint PUT /{operationId}/complete
 */
public class FiltrationCompletionDto {

    @NotNull(message = "Le volume après filtration est requis")
    @DecimalMin(value = "0.0", message = "Le volume après filtration doit être positif")
    private Double volumeAfter;      // Volume après filtration (saisi par l'utilisateur)

    private String note;              // Note de completion (optionnelle)

    public Double getVolumeAfter() {
        return volumeAfter;
    }

    public String getNote() {
        return note;
    }

    public void setVolumeAfter(Double volumeAfter) {
        this.volumeAfter = volumeAfter;
    }

    public void setNote(String note) {
        this.note = note;
    }
}