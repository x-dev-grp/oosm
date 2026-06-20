package com.xdev.ooms.conditioning.shipping.entity;

import com.xdev.ooms.conditioning.shipping.enums.ShippingEventType;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ShippingEvent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_info_id", nullable = false)
    private ShippingInfo shippingInfo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ShippingEventType type;

    private LocalDateTime eventAt;

    @Column(length = 120)
    private String location;

    @Column(length = 1000)
    private String comment;

    public ShippingInfo getShippingInfo() {
        return shippingInfo;
    }

    public ShippingEventType getType() {
        return type;
    }

    public LocalDateTime getEventAt() {
        return eventAt;
    }

    public String getLocation() {
        return location;
    }

    public String getComment() {
        return comment;
    }

    public void setShippingInfo(ShippingInfo shippingInfo) {
        this.shippingInfo = shippingInfo;
    }

    public void setType(ShippingEventType type) {
        this.type = type;
    }

    public void setEventAt(LocalDateTime eventAt) {
        this.eventAt = eventAt;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

