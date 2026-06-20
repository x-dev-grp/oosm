package com.xdev.ooms.conditioning.qualitycontrol.dto;


import com.xdev.ooms.conditioning.Enum.ResultStatus;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import com.xdev.ooms.conditioning.qualitycontrol.entity.QCResult;
import java.time.LocalDateTime;
import java.util.UUID;

public class QCResultDTO extends BaseDto<QCResult> {
    private UUID controlPointId;
    private UUID ofId;
    private String valeur;
    private ResultStatus statut;
    private String commentaire;
    private String photo;
    private String signature;
    private LocalDateTime dateControle;

    public UUID getControlPointId() {
        return controlPointId;
    }

    public UUID getOfId() {
        return ofId;
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

    public void setControlPointId(UUID controlPointId) {
        this.controlPointId = controlPointId;
    }

    public void setOfId(UUID ofId) {
        this.ofId = ofId;
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