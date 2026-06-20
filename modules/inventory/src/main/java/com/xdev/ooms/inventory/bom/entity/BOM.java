package com.xdev.ooms.inventory.bom.entity;

import com.xdev.ooms.inventory.produitfinal.entity.ProduitFinal;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Data
@Getter
@Setter
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
}
