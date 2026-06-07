package com.xdev.ooms.sharedkernel.dtos;

import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import com.xdev.ooms.sharedkernel.models.Action;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class BaseDto<E extends BaseEntity> implements Serializable {
    private UUID id;
    private UUID tenantId;
    private Set<Action> actions = Collections.emptySet();
    private Boolean isDeleted = false;
    private UUID externalId;
    private LocalDateTime createdDate;
    private String createdBy;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
    private String qrHex;

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public Set<Action> getActions() {
        return actions;
    }

    public void setActions(Set<Action> actions) {
        this.actions = actions;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getExternalId() {
        return externalId;
    }

    public void setExternalId(UUID externalId) {
        this.externalId = externalId;
    }

    public String getQrHex() {
        return qrHex;
    }

    public void setQrHex(String qrHex) {
        this.qrHex = qrHex;
    }
}
