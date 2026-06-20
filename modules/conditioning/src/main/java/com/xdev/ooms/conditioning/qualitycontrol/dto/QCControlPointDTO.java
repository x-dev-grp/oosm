package com.xdev.ooms.conditioning.qualitycontrol.dto;


import com.xdev.ooms.conditioning.Enum.ControlType;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import com.xdev.ooms.conditioning.qualitycontrol.entity.QCControlPoint;
import java.util.UUID;

public class QCControlPointDTO extends BaseDto<QCControlPoint> {
    private UUID planId;
    private String nom;
    private ControlType type;
    private Double minValue;
    private Double maxValue;
    private boolean blocking;

    public UUID getPlanId() {
        return planId;
    }

    public String getNom() {
        return nom;
    }

    public ControlType getType() {
        return type;
    }

    public Double getMinValue() {
        return minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public boolean isBlocking() {
        return blocking;
    }

    public void setPlanId(UUID planId) {
        this.planId = planId;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setType(ControlType type) {
        this.type = type;
    }

    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }
}