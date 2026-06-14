package com.xdev.ooms.production.genealogy.dto;

import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;

import com.xdev.ooms.production.unifieddelivery.dto.UnifiedDeliveryDTO;

import lombok.Data;

import java.util.List;

@Data
public class GlobalLotDto {
    private String globalLotNumber;
    private double totalKg;
    private List<UnifiedDeliveryDTO> lots;

    // Constructor for getPlanning
    public GlobalLotDto(String globalLotNumber, double totalKg, List<UnifiedDeliveryDTO> lots) {
        this.globalLotNumber = globalLotNumber;
        this.totalKg = totalKg;
        this.lots = lots;
    }
}