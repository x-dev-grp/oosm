package com.xdev.ooms.production.filtration.dto;


import com.xdev.ooms.production.filtration.dto.FiltrationStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateFiltrationStatusDto {

    @NotNull(message = "Le statut est requis")
    private FiltrationStatus status;  // Nouveau statut

    // [NOUVEAU] Ajout de la note optionnelle
    private String note;              // Note à ajouter lors du changement de statut
}