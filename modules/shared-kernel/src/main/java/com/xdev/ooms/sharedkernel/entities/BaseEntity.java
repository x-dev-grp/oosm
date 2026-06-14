package com.xdev.ooms.sharedkernel.entities;

import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.utils.AuditEntityListener;
import jakarta.persistence.*;
import org.hibernate.annotations.NaturalId;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;


@Audited
@MappedSuperclass
@EntityListeners(AuditEntityListener.class)
public class BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID tenantId;
    private Boolean isDeleted = false;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @Column(updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedBy
    private String lastModifiedBy;

    private LocalDateTime lastModifiedDate;

    @NaturalId
    private UUID externalId;


    public UUID getExternalId() {
        return externalId;
    }

    public void setExternalId(UUID externalId) {
        this.externalId = externalId;
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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }


    //-------------Qr code-------------//
    @Column(name = "qr_hex", length = 6)
    private String qrHex;

    @Column(name = "qr_image_base64", columnDefinition = "TEXT")
    private String qrImageBase64;


    public String getQrHex() {
        return qrHex;
    }

    public void setQrHex(String qrHex) {
        this.qrHex = qrHex;
    }
    public String getQrImageBase64() {
        return qrImageBase64;
    }

    public void setQrImageBase64(String qrImageBase64) {
        this.qrImageBase64 = qrImageBase64;
    }

    //-------------Qr code-------------//



    @PrePersist
    protected void onCreate() {
        if (externalId == null) {
            externalId = UUID.randomUUID();
        }
        if( getTenantId() == null ) {
            setTenantId(TenantContext.getCurrentTenant());
        }
        setCreatedDate(LocalDateTime.now());
        setLastModifiedDate(LocalDateTime.now());
    }

    @PreUpdate
    protected void onUpdate() {
        if (externalId == null) {
            externalId = UUID.randomUUID();
        }
        if( getTenantId() == null ) {
            setTenantId(TenantContext.getCurrentTenant());
        }
        setLastModifiedDate(LocalDateTime.now());
    }

}
