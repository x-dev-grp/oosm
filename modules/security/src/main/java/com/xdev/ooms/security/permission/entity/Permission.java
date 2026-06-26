package com.xdev.ooms.security.permission.entity;

import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import com.xdev.ooms.sharedkernel.models.OOSMModule;
import jakarta.persistence.Entity;

@Entity
public class Permission extends BaseEntity {
    private String permissionName;
    private OOSMModule module;
    private String entity;

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public OOSMModule getModule() {
        return module;
    }

    public void setModule(OOSMModule module) {
        this.module = module;
    }

}
