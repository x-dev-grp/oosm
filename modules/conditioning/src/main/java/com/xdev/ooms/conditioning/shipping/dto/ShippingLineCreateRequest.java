package com.xdev.ooms.conditioning.shipping.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class ShippingLineCreateRequest {

    @NotNull
    private UUID articleId;

    @NotNull
    @Min(1)
    private Integer quantity;

    private String unit;

    public UUID getArticleId() {
        return articleId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setArticleId(UUID articleId) {
        this.articleId = articleId;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}

