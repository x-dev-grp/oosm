package com.xdev.ooms.inventory.bom.dto;

import com.xdev.ooms.inventory.articlesec.entity.ArticleSec;
import com.xdev.ooms.inventory.bom.entity.BOM;
import com.xdev.ooms.inventory.bom.entity.BomLine;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import com.xdev.ooms.sharedkernel.models.UniteMesure;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public class BomLineDto  extends BaseDto<BomLine> implements Serializable {
    private BOM bom;
    private ArticleSec article;
    private UUID articleId;
    private String articleName;
    private double quantity;
    private UniteMesure unitOfMeasure;


    public BOM getBom() {
        return bom;
    }

    public ArticleSec getArticle() {
        return article;
    }

    public UUID getArticleId() {
        return articleId;
    }

    public String getArticleName() {
        return articleName;
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

    public void setArticleId(UUID articleId) {
        this.articleId = articleId;
    }

    public void setArticleName(String articleName) {
        this.articleName = articleName;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public void setUnitOfMeasure(UniteMesure unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }
}