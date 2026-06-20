package com.xdev.ooms.inventory.boncommande.dto;

import com.xdev.ooms.inventory.Enum.StatutBonCommande;
import com.xdev.ooms.inventory.boncommande.entity.BonCommande;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class BonCommandeDto extends BaseDto<BonCommande> implements Serializable {

    private UUID id;
    private UUID tenantId;
    private Boolean isDeleted;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
    private UUID externalId;
    private String numeroBC;
    private LocalDateTime dateValidation;
    private LocalDateTime dateReceptionPrevue;
    private StatutBonCommande status;
    private List<LigneBonCommandeDto> lignes;
    private String motifRefus;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public UUID getExternalId() {
        return externalId;
    }

    public void setExternalId(UUID externalId) {
        this.externalId = externalId;
    }

    public String getNumeroBC() {
        return numeroBC;
    }

    public void setNumeroBC(String numeroBC) {
        this.numeroBC = numeroBC;
    }

    public LocalDateTime getDateValidation() {
        return dateValidation;
    }

    public void setDateValidation(LocalDateTime dateValidation) {
        this.dateValidation = dateValidation;
    }

    public LocalDateTime getDateReceptionPrevue() {
        return dateReceptionPrevue;
    }

    public void setDateReceptionPrevue(LocalDateTime dateReceptionPrevue) {
        this.dateReceptionPrevue = dateReceptionPrevue;
    }

    public StatutBonCommande getStatus() {
        return status;
    }

    public void setStatus(StatutBonCommande status) {
        this.status = status;
    }

    public List<LigneBonCommandeDto> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneBonCommandeDto> lignes) {
        this.lignes = lignes;
    }

    public String getMotifRefus() {
        return motifRefus;
    }

    public void setMotifRefus(String motifRefus) {
        this.motifRefus = motifRefus;
    }
}
