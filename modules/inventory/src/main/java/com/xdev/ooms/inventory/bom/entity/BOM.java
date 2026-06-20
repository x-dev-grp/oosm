package com.xdev.ooms.inventory.bom.entity;

import com.xdev.ooms.inventory.produitfinal.entity.ProduitFinal;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class BOM extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "sku_id")
    private ProduitFinal produitFinal;
    private String version;

    @Column(nullable = false)
    private boolean active = false;

    @OneToMany(mappedBy = "bom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BomLine> lines = new ArrayList<>();

    public ProduitFinal getProduitFinal() {
        return produitFinal;
    }

    public String getVersion() {
        return version;
    }

    public boolean isActive() {
        return active;
    }

    public List<BomLine> getLines() {
        return lines;
    }

    public void setProduitFinal(ProduitFinal produitFinal) {
        this.produitFinal = produitFinal;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setLines(List<BomLine> lines) {
        this.lines = lines;
    }
}
