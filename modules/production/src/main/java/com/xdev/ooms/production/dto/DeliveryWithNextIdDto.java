package com.xdev.ooms.production.dto;

import com.xdev.ooms.production.model.UnifiedDelivery;

public class DeliveryWithNextIdDto {
    private final UnifiedDelivery delivery;
    private final Long nextId;

    public DeliveryWithNextIdDto(UnifiedDelivery delivery, Long nextId) {
        this.delivery = delivery;
        this.nextId = nextId;
    }

    public UnifiedDelivery getDelivery() {
        return delivery;
    }

    public Long getNextId() {
        return nextId;
    }
}
