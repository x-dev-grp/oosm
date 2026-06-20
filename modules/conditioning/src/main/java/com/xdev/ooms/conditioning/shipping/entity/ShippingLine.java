package com.xdev.ooms.conditioning.shipping.entity;

import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class ShippingLine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_info_id", nullable = false)
    private ShippingInfo shippingInfo;

    private UUID articleId;

    private String articleNameSnapshot;

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 30)
    private String unit;

    public ShippingInfo getShippingInfo() {
        return shippingInfo;
    }

    public UUID getArticleId() {
        return articleId;
    }

    public String getArticleNameSnapshot() {
        return articleNameSnapshot;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setShippingInfo(ShippingInfo shippingInfo) {
        this.shippingInfo = shippingInfo;
    }

    public void setArticleId(UUID articleId) {
        this.articleId = articleId;
    }

    public void setArticleNameSnapshot(String articleNameSnapshot) {
        this.articleNameSnapshot = articleNameSnapshot;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}

