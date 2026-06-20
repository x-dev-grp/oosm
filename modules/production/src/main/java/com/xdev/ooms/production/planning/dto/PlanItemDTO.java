package com.xdev.ooms.production.planning.dto;

import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;

import com.xdev.ooms.production.unifieddelivery.dto.UnifiedDeliveryDTO;

public class PlanItemDTO {
    private String type; // "LOT" or "GLOBAL_LOT"
    private String id; // lotNumber for LOT, globalLotNumber for GLOBAL_LOT
    private UnifiedDeliveryDTO lot; // Used in getPlanning response

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public UnifiedDeliveryDTO getLot() {
        return lot;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLot(UnifiedDeliveryDTO lot) {
        this.lot = lot;
    }
}