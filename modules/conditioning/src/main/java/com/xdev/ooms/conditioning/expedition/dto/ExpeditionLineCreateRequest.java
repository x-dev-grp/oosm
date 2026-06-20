package com.xdev.ooms.conditioning.expedition.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.UUID;

public class ExpeditionLineCreateRequest {

    private UUID ofId;
    private UUID articleId;
    @Min(1)
    private Integer quantity;
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal volume;
    private String lotNumber;
    private String unit;

    public UUID getOfId() {
        return ofId;
    }

    public UUID getArticleId() {
        return articleId;
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

    public void setArticleId(UUID articleId) {
        this.articleId = articleId;
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
