package com.xdev.ooms.conditioning.qualitycontrol.entity;


import com.xdev.ooms.conditioning.Enum.ControlType;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import java.io.Serializable;
@Entity
public class QCControlPoint extends BaseEntity implements Serializable {

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private QCPlan plan;
    private String nom;
    @Enumerated(EnumType.STRING)
    private ControlType type;
    private Double minValue;
    private Double maxValue;
    private boolean blocking;



    public QCPlan getPlan() {
        return plan;
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

    public void setPlan(QCPlan plan) {
        this.plan = plan;
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