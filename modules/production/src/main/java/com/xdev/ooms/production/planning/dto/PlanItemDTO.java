package com.xdev.ooms.production.planning.dto;

import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;

import com.xdev.ooms.production.unifieddelivery.dto.UnifiedDeliveryDTO;

import lombok.Data;

@Data
public class PlanItemDTO {
    private String type; // "LOT" or "GLOBAL_LOT"
    private String id; // lotNumber for LOT, globalLotNumber for GLOBAL_LOT
    private UnifiedDeliveryDTO lot; // Used in getPlanning response
}