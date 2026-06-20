package com.xdev.ooms.conditioning.ordrefabrication.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class AjustementConsommationDto {
    private UUID articleId;
    private BigDecimal quantiteReelle;
    private String motif;

    public UUID getArticleId() {
        return articleId;
    }

    public BigDecimal getQuantiteReelle() {
        return quantiteReelle;
    }

    public String getMotif() {
        return motif;
    }

    public void setArticleId(UUID articleId) {
        this.articleId = articleId;
    }

    public void setQuantiteReelle(BigDecimal quantiteReelle) {
        this.quantiteReelle = quantiteReelle;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }
}