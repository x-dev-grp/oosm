package com.xdev.ooms.conditioning.shipping.dto;

import com.xdev.ooms.conditioning.shipping.enums.ShippingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShippingStatusUpdateRequest {

    @NotNull
    private ShippingStatus status;

    private String location;
    private String comment;
}

