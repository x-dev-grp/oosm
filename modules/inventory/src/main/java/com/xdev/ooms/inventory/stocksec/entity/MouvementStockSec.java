package com.xdev.ooms.inventory.stocksec.entity;

import com.xdev.ooms.inventory.articlesec.entity.ArticleSec;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import com.xdev.ooms.inventory.Enum.TypeMouvement;

@Entity
public class MouvementStockSec  extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private ArticleSec article;

    @Column(nullable = false)
    private Integer quantite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeMouvement typeMouvement;

    private String motif;

    private LocalDateTime dateMouvement;

    @Column(name = "reference_type")
    private String referenceType;

    @Column(name = "reference_id")
    private UUID referenceId;


    public MouvementStockSec() {
    }

    public MouvementStockSec(ArticleSec article, Integer quantite, TypeMouvement typeMouvement, String motif, LocalDateTime dateMouvement, String referenceType, UUID referenceId) {
        this.article = article;
        this.quantite = quantite;
        this.typeMouvement = typeMouvement;
        this.motif = motif;
        this.dateMouvement = dateMouvement;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
    }

    public ArticleSec getArticle() {
        return article;
    }

    public Integer getQuantite() {
        return quantite;
    }

    public TypeMouvement getTypeMouvement() {
        return typeMouvement;
    }

    public String getMotif() {
        return motif;
    }

    public LocalDateTime getDateMouvement() {
        return dateMouvement;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public void setArticle(ArticleSec article) {
        this.article = article;
    }

    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
    }

    public void setTypeMouvement(TypeMouvement typeMouvement) {
        this.typeMouvement = typeMouvement;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public void setDateMouvement(LocalDateTime dateMouvement) {
        this.dateMouvement = dateMouvement;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public void setReferenceId(UUID referenceId) {
        this.referenceId = referenceId;
    }
}