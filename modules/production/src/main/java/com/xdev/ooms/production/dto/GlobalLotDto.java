package com.xdev.ooms.production.dto;

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