package com.xdev.ooms.conditioning.shipping.dto;

import com.xdev.ooms.conditioning.shipping.enums.ShippingEventType;
import com.xdev.ooms.conditioning.shipping.model.ShippingEvent;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShippingEventDto extends BaseDto<ShippingEvent> {
    private ShippingEventType type;
    private LocalDateTime eventAt;
    private String location;
    private String comment;
}
