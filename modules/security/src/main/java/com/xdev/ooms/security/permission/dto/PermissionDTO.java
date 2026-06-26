package com.xdev.ooms.security.permission.dto;

import com.xdev.ooms.security.permission.entity.Permission;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import com.xdev.ooms.sharedkernel.models.OOSMModule;


public class PermissionDTO extends BaseDto<Permission> {
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
