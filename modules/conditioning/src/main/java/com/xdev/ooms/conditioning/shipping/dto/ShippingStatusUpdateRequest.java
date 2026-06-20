package com.xdev.ooms.conditioning.shipping.dto;

import com.xdev.ooms.conditioning.shipping.enums.ShippingStatus;
import jakarta.validation.constraints.NotNull;
public class ShippingStatusUpdateRequest {

    @NotNull
    private ShippingStatus status;

    private String location;
    private String comment;

    public ShippingStatus getStatus() {
        return status;
    }

    public String getLocation() {
        return location;
    }

    public String getComment() {
        return comment;
    }

    public void setStatus(ShippingStatus status) {
        this.status = status;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

