package com.xdev.ooms.conditioning.shipping.dto;

import com.xdev.ooms.conditioning.shipping.model.ShippingLine;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import lombok.Data;

import java.util.UUID;

@Data
public class ShippingLineDto extends BaseDto<ShippingLine> {
    private UUID articleId;
    private String articleName;
    private Integer quantity;
    private String unit;
}
