package com.xdev.ooms.conditioning.inventoryusage.dto;

import com.xdev.ooms.inventory.articlesec.dto.ArticleSecDto;
import com.xdev.ooms.inventory.bom.dto.BOMDto;
import com.xdev.ooms.sharedkernel.models.UniteMesure;
import java.util.UUID;
public class BomLineDto  {
    private UUID id;
    private double quantity;

    private BOMDto bom;
    private ArticleSecDto article;
    private UniteMesure unitOfMeasure;

    public UUID getId() {
        return id;
    }

    public double getQuantity() {
        return quantity;
    }

    public BOMDto getBom() {
        return bom;
    }

    public ArticleSecDto getArticle() {
        return article;
    }

    public UniteMesure getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public void setBom(BOMDto bom) {
        this.bom = bom;
    }

    public void setArticle(ArticleSecDto article) {
        this.article = article;
    }

    public void setUnitOfMeasure(UniteMesure unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }
}
