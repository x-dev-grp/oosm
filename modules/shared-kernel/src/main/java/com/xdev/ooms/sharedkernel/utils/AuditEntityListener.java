package com.xdev.ooms.sharedkernel.utils;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class AuditEntityListener {

    @PrePersist
    public void prePersist(Object entity) {
        AuditHelper.applyAuditOnCreate(entity);
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        AuditHelper.applyAuditOnUpdate(entity);
    }
}