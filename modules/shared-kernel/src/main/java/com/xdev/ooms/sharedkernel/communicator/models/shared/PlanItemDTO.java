package com.xdev.ooms.sharedkernel.communicator.models.shared;


public class PlanItemDTO {
    private String type; // "LOT" or "GLOBAL_LOT"
    private String id; // lotNumber for LOT, globalLotNumber for GLOBAL_LOT
    private UnifiedDeliveryDTO lot; // Used in getPlanning response
}