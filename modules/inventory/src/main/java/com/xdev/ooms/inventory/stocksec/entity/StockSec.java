package com.xdev.ooms.inventory.stocksec.entity;

import com.xdev.ooms.inventory.articlesec.entity.ArticleSec;
import com.xdev.ooms.inventory.emplacementstock.entity.EmplacementStock;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class StockSec extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "article_id", unique = true)
    private ArticleSec article;

    @Column(name = "quantite_actuelle", nullable = false)
    private Integer quantiteActuelle = 0;

    @ManyToOne
    @JoinColumn(name = "emplacement_id")
    private EmplacementStock emplacement;

    //expedition
    @Column(name = "reserve_pour")
    private String reservePour;

    @Column(name = "reserve_date")
    private LocalDateTime reserveDate;

    @Column(name = "quantite_reservee", nullable = false)
    private Integer quantiteReservee = 0;

    @Column(name = "quantite_disponible")
    private Integer quantiteDisponible;


    public StockSec() {
    }

    public StockSec(ArticleSec article, Integer quantiteActuelle, EmplacementStock emplacement, String reservePour, LocalDateTime reserveDate, Integer quantiteReservee, Integer quantiteDisponible) {
        this.article = article;
        this.quantiteActuelle = quantiteActuelle;
        this.emplacement = emplacement;
        this.reservePour = reservePour;
        this.reserveDate = reserveDate;
        this.quantiteReservee = quantiteReservee;
        this.quantiteDisponible = quantiteDisponible;
    }

    public ArticleSec getArticle() {
        return article;
    }

    public Integer getQuantiteActuelle() {
        return quantiteActuelle;
    }

    public EmplacementStock getEmplacement() {
        return emplacement;
    }

    public String getReservePour() {
        return reservePour;
    }

    public LocalDateTime getReserveDate() {
        return reserveDate;
    }

    public Integer getQuantiteReservee() {
        return quantiteReservee;
    }

    public Integer getQuantiteDisponible() {
        return quantiteDisponible;
    }

    public void setArticle(ArticleSec article) {
        this.article = article;
    }

    public void setQuantiteActuelle(Integer quantiteActuelle) {
        this.quantiteActuelle = quantiteActuelle;
    }

    public void setEmplacement(EmplacementStock emplacement) {
        this.emplacement = emplacement;
    }

    public void setReservePour(String reservePour) {
        this.reservePour = reservePour;
    }

    public void setReserveDate(LocalDateTime reserveDate) {
        this.reserveDate = reserveDate;
    }

    public void setQuantiteReservee(Integer quantiteReservee) {
        this.quantiteReservee = quantiteReservee;
    }

    public void setQuantiteDisponible(Integer quantiteDisponible) {
        this.quantiteDisponible = quantiteDisponible;
    }
}