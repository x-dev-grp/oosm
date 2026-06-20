package com.xdev.ooms.production.genealogy.dto;

import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;

import com.xdev.ooms.production.unifieddelivery.dto.UnifiedDeliveryDTO;

import java.util.List;

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

    public String getGlobalLotNumber() {
        return globalLotNumber;
    }

    public double getTotalKg() {
        return totalKg;
    }

    public List<UnifiedDeliveryDTO> getLots() {
        return lots;
    }

    public void setGlobalLotNumber(String globalLotNumber) {
        this.globalLotNumber = globalLotNumber;
    }

    public void setTotalKg(double totalKg) {
        this.totalKg = totalKg;
    }

    public void setLots(List<UnifiedDeliveryDTO> lots) {
        this.lots = lots;
    }
}