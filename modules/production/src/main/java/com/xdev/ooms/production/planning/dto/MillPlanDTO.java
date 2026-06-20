package com.xdev.ooms.production.planning.dto;


import com.xdev.ooms.production.planning.dto.PlanItemDTO;

import java.util.List;
import java.util.UUID;

public class MillPlanDTO {
    private UUID millMachineId;
    private List<PlanItemDTO> items;

    // Constructor for getPlanning
    public MillPlanDTO(UUID millMachineId, List<PlanItemDTO> items) {
        this.millMachineId = millMachineId;
        this.items = items;
    }

    public UUID getMillMachineId() {
        return millMachineId;
    }

    public List<PlanItemDTO> getItems() {
        return items;
    }

    public void setMillMachineId(UUID millMachineId) {
        this.millMachineId = millMachineId;
    }

    public void setItems(List<PlanItemDTO> items) {
        this.items = items;
    }
}