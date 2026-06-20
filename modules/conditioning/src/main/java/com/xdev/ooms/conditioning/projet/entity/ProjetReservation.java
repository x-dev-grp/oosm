package com.xdev.ooms.conditioning.projet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
public class ProjetReservation extends BaseEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id", nullable = false)
    @JsonIgnore
    private Projet projet;

    @Column(name = "article_id", nullable = false)
    private UUID articleId;

    @Column(name = "quantite_reservee", nullable = false)
    private Double quantiteReservee;
    
    // Status can be e.g. "PENDING", "CONFIRMED", "RELEASED"
    private String statut;

    public Projet getProjet() {
        return projet;
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

    public void setProjet(Projet projet) {
        this.projet = projet;
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
