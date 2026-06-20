package com.xdev.ooms.sharedkernel.dtos;

import java.time.LocalDateTime;

public class AuditDto {
    private String entityName;
    private String id;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
    private Integer revision;
    private String revisionType;

    public AuditDto() {
    }

    public AuditDto(String entityName, String id, String createdBy, LocalDateTime createdDate, String lastModifiedBy, LocalDateTime lastModifiedDate, Integer revision, String revisionType) {
        this.entityName = entityName;
        this.id = id;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.lastModifiedBy = lastModifiedBy;
        this.lastModifiedDate = lastModifiedDate;
        this.revision = revision;
        this.revisionType = revisionType;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getId() {
        return id;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public Integer getRevision() {
        return revision;
    }

    public String getRevisionType() {
        return revisionType;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public void setRevision(Integer revision) {
        this.revision = revision;
    }

    public void setRevisionType(String revisionType) {
        this.revisionType = revisionType;
    }
}