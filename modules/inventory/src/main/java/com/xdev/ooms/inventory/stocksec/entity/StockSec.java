package com.xdev.ooms.inventory.stocksec.entity;

import com.xdev.ooms.inventory.articlesec.entity.ArticleSec;
import com.xdev.ooms.inventory.emplacementstock.entity.EmplacementStock;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
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

}