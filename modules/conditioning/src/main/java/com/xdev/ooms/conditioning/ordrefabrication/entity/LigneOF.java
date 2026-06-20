package com.xdev.ooms.conditioning.ordrefabrication.entity;

import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
public class LigneOF extends BaseEntity implements Serializable {

    @ManyToOne
    @JoinColumn(name = "of_id", nullable = false)
    private OrdreFabrication of;

    @Column(name = "article_id", nullable = false)
    private UUID articleId;

    @Column(nullable = false)
    private BigDecimal quantiteTheorique;

    private BigDecimal quantiteReelle;

    private String motifAjustement;

    public OrdreFabrication getOf() {
        return of;
    }

    public UUID getArticleId() {
        return articleId;
    }

    public BigDecimal getQuantiteTheorique() {
        return quantiteTheorique;
    }

    public BigDecimal getQuantiteReelle() {
        return quantiteReelle;
    }

    public String getMotifAjustement() {
        return motifAjustement;
    }

    public void setOf(OrdreFabrication of) {
        this.of = of;
    }

    public void setArticleId(UUID articleId) {
        this.articleId = articleId;
    }

    public void setQuantiteTheorique(BigDecimal quantiteTheorique) {
        this.quantiteTheorique = quantiteTheorique;
    }

    public void setQuantiteReelle(BigDecimal quantiteReelle) {
        this.quantiteReelle = quantiteReelle;
    }

    public void setMotifAjustement(String motifAjustement) {
        this.motifAjustement = motifAjustement;
    }
}