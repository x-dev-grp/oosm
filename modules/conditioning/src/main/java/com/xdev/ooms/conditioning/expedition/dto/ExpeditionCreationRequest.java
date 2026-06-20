package com.xdev.ooms.conditioning.expedition.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExpeditionCreationRequest {

    @NotNull
    private UUID projetId;

    private String destination;
    private LocalDate plannedShipDate;
    private String notes;
    private List<ExpeditionLineCreateRequest> lines = new ArrayList<>();

    public UUID getProjetId() {
        return projetId;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDate getPlannedShipDate() {
        return plannedShipDate;
    }

    public String getNotes() {
        return notes;
    }

    public List<ExpeditionLineCreateRequest> getLines() {
        return lines;
    }

    public void setProjetId(UUID projetId) {
        this.projetId = projetId;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setPlannedShipDate(LocalDate plannedShipDate) {
        this.plannedShipDate = plannedShipDate;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setLines(List<ExpeditionLineCreateRequest> lines) {
        this.lines = lines;
    }
}
