package com.xdev.ooms.conditioning.projet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
public class ProjetProduit extends BaseEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id", nullable = false)
    @JsonIgnore
    private Projet projet;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "bom_id")
    private UUID bomId;

    @Column(name = "quantite_cible", nullable = false)
    private Double quantiteCible;

    public Projet getProjet() {
        return projet;
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getBomId() {
        return bomId;
    }

    public Double getQuantiteCible() {
        return quantiteCible;
    }

    public void setProjet(Projet projet) {
        this.projet = projet;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public void setBomId(UUID bomId) {
        this.bomId = bomId;
    }

    public void setQuantiteCible(Double quantiteCible) {
        this.quantiteCible = quantiteCible;
    }
}
