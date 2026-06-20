package com.xdev.ooms.conditioning.projet.dto;

import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import java.util.UUID;

public class ProjetReservationDto extends BaseDto<com.xdev.ooms.conditioning.projet.entity.ProjetReservation> {
    private UUID projetId;
    private UUID articleId;
    private Double quantiteReservee;
    private String statut;

    public UUID getProjetId() {
        return projetId;
    }

    public UUID getArticleId() {
        return articleId;
    }

    public Double getQuantiteReservee() {
        return quantiteReservee;
    }

    public String getStatut() {
        return statut;
    }

    public void setProjetId(UUID projetId) {
        this.projetId = projetId;
    }

    public void setArticleId(UUID articleId) {
        this.articleId = articleId;
    }

    public void setQuantiteReservee(Double quantiteReservee) {
        this.quantiteReservee = quantiteReservee;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }
}
