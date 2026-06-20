package com.xdev.ooms.conditioning.qualitycontrol.dto;

import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import com.xdev.ooms.conditioning.qualitycontrol.entity.QCPlan;
import java.util.List;
import java.util.UUID;

public class QCPlanDTO extends BaseDto<QCPlan> {
    private UUID ofId;
    private UUID id;
    private String titre;
    private boolean actif;
    private List<QCControlPointDTO> points;  // ou simplement List<UUID> pointIds

    public UUID getOfId() {
        return ofId;
    }

    public UUID getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public boolean isActif() {
        return actif;
    }

    public List<QCControlPointDTO> getPoints() {
        return points;
    }

    public void setOfId(UUID ofId) {
        this.ofId = ofId;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public void setPoints(List<QCControlPointDTO> points) {
        this.points = points;
    }
}