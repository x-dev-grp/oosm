package com.xdev.ooms.conditioning.shipping.dto;

import com.xdev.ooms.conditioning.shipping.entity.ShippingEvent;
import com.xdev.ooms.conditioning.shipping.enums.ShippingEventType;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import java.time.LocalDateTime;

public class ShippingEventDto extends BaseDto<ShippingEvent> {
    private ShippingEventType type;
    private LocalDateTime eventAt;
    private String location;
    private String comment;

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
