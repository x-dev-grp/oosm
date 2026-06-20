package com.xdev.ooms.conditioning.shipping.dto;

import com.xdev.ooms.conditioning.shipping.entity.ShippingLine;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import java.util.UUID;

public class ShippingLineDto extends BaseDto<ShippingLine> {
    private UUID articleId;
    private String articleName;
    private Integer quantity;
    private String unit;

    public UUID getArticleId() {
        return articleId;
    }

    public String getArticleName() {
        return articleName;
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

    public void setArticleName(String articleName) {
        this.articleName = articleName;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
