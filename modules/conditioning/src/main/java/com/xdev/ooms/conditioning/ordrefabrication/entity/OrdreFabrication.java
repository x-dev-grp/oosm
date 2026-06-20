package com.xdev.ooms.conditioning.ordrefabrication.entity;


import com.xdev.ooms.conditioning.Enum.QualityStatus;
import com.xdev.ooms.conditioning.Enum.StatutOF;
import com.xdev.ooms.conditioning.projet.entity.Projet;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Getter
@Setter
public class OrdreFabrication extends BaseEntity implements Serializable {

    @Column(unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutOF statut = StatutOF.PLANIFIE;

    private LocalDateTime dateDebutPrevue;
    private LocalDateTime dateFinPrevue;
    private LocalDateTime dateDebutReelle;
    private LocalDateTime dateFinReelle;

    @Column(nullable = false)
    private BigDecimal quantiteCible;

    private BigDecimal quantiteBonne = BigDecimal.ZERO;
    private BigDecimal quantiteNC = BigDecimal.ZERO;
    private Long dureeReelle;
    @Column(name = "sku_id", nullable = false)
    private UUID productId;
    @Column(name = "bom_id")
    private UUID bomId;
    @Column(name = "ligne_id")
    private UUID ligneId;

    @Column(name = "lot_vrac_id")
    private UUID lotVracId;

    @Column(name = "traceability_lot_id")
    private UUID traceabilityLotId;

    private String motifNC;

    @OneToMany(mappedBy = "of", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneOF> lignes = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private QualityStatus qualityStatus = QualityStatus.FREE;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id")
    private Projet projet;

    public UUID getSkuId() {
        return productId;
    }

    public void setSkuId(UUID skuId) {
        this.productId = skuId;
    }
}
