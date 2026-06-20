package com.xdev.ooms.inventory.bom.entity;

 import com.xdev.ooms.inventory.articlesec.entity.ArticleSec;
 import com.xdev.ooms.sharedkernel.entities.BaseEntity;
 import com.xdev.ooms.sharedkernel.models.UniteMesure;
 import jakarta.persistence.*;
@Entity
public class BomLine extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "bom_id")
    private BOM bom;

    @ManyToOne
    @JoinColumn(name = "articleSec_id")
    private ArticleSec article;

    private double quantity;
    @Enumerated(EnumType.STRING)
    private UniteMesure unitOfMeasure;

    public BOM getBom() {
        return bom;
    }

    public ArticleSec getArticle() {
        return article;
    }

    public double getQuantity() {
        return quantity;
    }

    public UniteMesure getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setBom(BOM bom) {
        this.bom = bom;
    }

    public void setArticle(ArticleSec article) {
        this.article = article;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public void setUnitOfMeasure(UniteMesure unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }
}