package com.xdev.ooms.inventory.stocksec.dto;

import com.xdev.ooms.inventory.Enum.TypeMouvement;
import java.time.LocalDateTime;
import java.util.UUID;

public class MouvementRecentDto {
    private UUID id;
    private TypeMouvement typeMouvement;
    private Integer quantite;
    private LocalDateTime dateMouvement;
    private String motif;
    private UUID articleId;
    private String articleSku;
    private String articleNom;
    private String uniteMesure;

    public MouvementRecentDto() {
    }

    public MouvementRecentDto(UUID id, TypeMouvement typeMouvement, Integer quantite, LocalDateTime dateMouvement, String motif, UUID articleId, String articleSku, String articleNom, String uniteMesure) {
        this.id = id;
        this.typeMouvement = typeMouvement;
        this.quantite = quantite;
        this.dateMouvement = dateMouvement;
        this.motif = motif;
        this.articleId = articleId;
        this.articleSku = articleSku;
        this.articleNom = articleNom;
        this.uniteMesure = uniteMesure;
    }

    public UUID getId() {
        return id;
    }

    public TypeMouvement getTypeMouvement() {
        return typeMouvement;
    }

    public Integer getQuantite() {
        return quantite;
    }

    public LocalDateTime getDateMouvement() {
        return dateMouvement;
    }

    public String getMotif() {
        return motif;
    }

    public UUID getArticleId() {
        return articleId;
    }

    public String getArticleSku() {
        return articleSku;
    }

    public String getArticleNom() {
        return articleNom;
    }

    public String getUniteMesure() {
        return uniteMesure;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setTypeMouvement(TypeMouvement typeMouvement) {
        this.typeMouvement = typeMouvement;
    }

    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
    }

    public void setDateMouvement(LocalDateTime dateMouvement) {
        this.dateMouvement = dateMouvement;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public void setArticleId(UUID articleId) {
        this.articleId = articleId;
    }

    public void setArticleSku(String articleSku) {
        this.articleSku = articleSku;
    }

    public void setArticleNom(String articleNom) {
        this.articleNom = articleNom;
    }

    public void setUniteMesure(String uniteMesure) {
        this.uniteMesure = uniteMesure;
    }
}
