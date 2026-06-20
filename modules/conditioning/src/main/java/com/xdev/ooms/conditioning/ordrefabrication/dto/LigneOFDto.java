package com.xdev.ooms.conditioning.ordrefabrication.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class LigneOFDto {
    private UUID articleId;
    private String articleNom;
    private BigDecimal quantiteTheorique;
    private BigDecimal quantiteReelle;
    private String motifAjustement;

    public UUID getArticleId() {
        return articleId;
    }

    public String getArticleNom() {
        return articleNom;
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

    public void setArticleId(UUID articleId) {
        this.articleId = articleId;
    }

    public void setArticleNom(String articleNom) {
        this.articleNom = articleNom;
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