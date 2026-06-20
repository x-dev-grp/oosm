package com.xdev.ooms.conditioning.expedition.dto;

import com.xdev.ooms.conditioning.expedition.entity.ExpeditionArticle;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import java.math.BigDecimal;
import java.util.UUID;

public class ExpeditionArticleDto extends BaseDto<ExpeditionArticle> {
    private UUID ofId;
    private String ofCode;
    private UUID articleId;
    private String articleName;
    private Integer quantity;
    private BigDecimal volume;
    private String lotNumber;
    private String unit;

    public UUID getOfId() {
        return ofId;
    }

    public String getOfCode() {
        return ofCode;
    }

    public UUID getArticleId() {
        return articleId;
    }

    public String getArticleName() {
        return articleName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public String getUnit() {
        return unit;
    }

    public void setOfId(UUID ofId) {
        this.ofId = ofId;
    }

    public void setOfCode(String ofCode) {
        this.ofCode = ofCode;
    }

    public void setArticleId(UUID articleId) {
        this.articleId = articleId;
    }

    public void setArticleName(String articleName) {
        this.articleName = articleName;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
