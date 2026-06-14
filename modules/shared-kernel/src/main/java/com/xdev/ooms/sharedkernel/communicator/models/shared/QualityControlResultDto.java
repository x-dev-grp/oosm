package com.xdev.ooms.sharedkernel.communicator.models.shared;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.xdev.ooms.sharedkernel.basetype.dto.BaseTypeDto;

import java.util.UUID;

/**
 * DTO for { QualityControlResult}
 */
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"}, ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QualityControlResultDto extends BaseTypeDto {

    QualityControlRuleDto rule;
    String measuredValue;
    private UUID deliveryId;


    public QualityControlRuleDto getRule() {
        return rule;
    }

    public void setRule(QualityControlRuleDto rule) {
        this.rule = rule;
    }

    public String getMeasuredValue() {
        return measuredValue;
    }

    public void setMeasuredValue(String measuredValue) {
        this.measuredValue = measuredValue;
    }

    public UUID getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(UUID deliveryId) {
        this.deliveryId = deliveryId;
    }
}
