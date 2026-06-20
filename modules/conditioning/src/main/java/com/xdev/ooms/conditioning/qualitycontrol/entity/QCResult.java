package com.xdev.ooms.conditioning.qualitycontrol.entity;


import com.xdev.ooms.conditioning.ordrefabrication.entity.OrdreFabrication;
import com.xdev.ooms.conditioning.Enum.ResultStatus;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
public class QCResult extends BaseEntity implements Serializable {

    @ManyToOne
    @JoinColumn(name = "control_point_id", nullable = false)
    private QCControlPoint controlPoint;
    @ManyToOne
    @JoinColumn(name = "of_id", nullable = false)
    private OrdreFabrication of;
    private String valeur;
    @Enumerated(EnumType.STRING)
    private ResultStatus statut;
    private String commentaire;
    @Column(columnDefinition = "TEXT")
    private String photo;
    private String signature;
    private LocalDateTime dateControle;



   
    public QCControlPoint getControlPoint() {
        return controlPoint;
    }

    public OrdreFabrication getOf() {
        return of;
    }

    public String getValeur() {
        return valeur;
    }

    public ResultStatus getStatut() {
        return statut;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public String getPhoto() {
        return photo;
    }

    public String getSignature() {
        return signature;
    }

    public LocalDateTime getDateControle() {
        return dateControle;
    }

    public void setControlPoint(QCControlPoint controlPoint) {
        this.controlPoint = controlPoint;
    }

    public void setOf(OrdreFabrication of) {
        this.of = of;
    }

    public void setValeur(String valeur) {
        this.valeur = valeur;
    }

    public void setStatut(ResultStatus statut) {
        this.statut = statut;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public void setDateControle(LocalDateTime dateControle) {
        this.dateControle = dateControle;
    }
}