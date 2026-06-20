package com.xdev.ooms.conditioning.qualitycontrol.entity;

import com.xdev.ooms.conditioning.ordrefabrication.entity.OrdreFabrication;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
public class QCPlan extends BaseEntity implements Serializable {

    @ManyToOne
    @JoinColumn(name = "of_id", nullable = false)
    private OrdreFabrication of;

    private String titre;
    private boolean actif = true;
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QCControlPoint> points = new ArrayList<>();
    public OrdreFabrication getOf() {
        return of;
    }

    public String getTitre() {
        return titre;
    }

    public boolean isActif() {
        return actif;
    }

    public List<QCControlPoint> getPoints() {
        return points;
    }

    public void setOf(OrdreFabrication of) {
        this.of = of;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public void setPoints(List<QCControlPoint> points) {
        this.points = points;
    }
}