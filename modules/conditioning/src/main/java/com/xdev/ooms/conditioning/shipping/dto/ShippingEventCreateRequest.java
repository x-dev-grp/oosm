package com.xdev.ooms.conditioning.shipping.dto;

import com.xdev.ooms.conditioning.shipping.enums.ShippingEventType;
import jakarta.validation.constraints.NotNull;
public class ShippingEventCreateRequest {

    @NotNull
    private ShippingEventType type;

    private String location;
    private String comment;

    public ShippingEventType getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }

    public String getComment() {
        return comment;
    }

    public void setType(ShippingEventType type) {
        this.type = type;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

