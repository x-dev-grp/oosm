package com.xdev.ooms.inventory.boncommande.entity;

import com.xdev.ooms.inventory.articlesec.entity.ArticleSec;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class LigneBonCommande extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JsonBackReference
    @JoinColumn(name = "bon_commande_id", nullable = false)
    private BonCommande bonCommande;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private ArticleSec article;

    @Column(nullable = false)
    private Integer quantiteCommandee;

    private Integer quantiteRecue = 0;

    private BigDecimal prixUnitaire;

    private String remarque;

    public LigneBonCommande() {
    }

    public LigneBonCommande(BonCommande bonCommande, ArticleSec article, Integer quantiteCommandee, Integer quantiteRecue, BigDecimal prixUnitaire, String remarque) {
        this.bonCommande = bonCommande;
        this.article = article;
        this.quantiteCommandee = quantiteCommandee;
        this.quantiteRecue = quantiteRecue;
        this.prixUnitaire = prixUnitaire;
        this.remarque = remarque;
    }

    public BonCommande getBonCommande() {
        return bonCommande;
    }

    public ArticleSec getArticle() {
        return article;
    }

    public Integer getQuantiteCommandee() {
        return quantiteCommandee;
    }

    public Integer getQuantiteRecue() {
        return quantiteRecue;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public String getRemarque() {
        return remarque;
    }

    public void setBonCommande(BonCommande bonCommande) {
        this.bonCommande = bonCommande;
    }

    public void setArticle(ArticleSec article) {
        this.article = article;
    }

    public void setQuantiteCommandee(Integer quantiteCommandee) {
        this.quantiteCommandee = quantiteCommandee;
    }

    public void setQuantiteRecue(Integer quantiteRecue) {
        this.quantiteRecue = quantiteRecue;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public void setRemarque(String remarque) {
        this.remarque = remarque;
    }
}