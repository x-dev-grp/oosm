package com.xdev.ooms.security.userManagement.dtos.OUTDTO;

import com.xdev.ooms.security.userManagement.models.Permission;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import com.xdev.ooms.sharedkernel.models.OSMModule;


public class PermissionDTO extends BaseDto<Permission> {
    private String permissionName;
    private OSMModule module;
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

    public OSMModule getModule() {
        return module;
    }

    public void setModule(OSMModule module) {
        this.module = module;
    }

}
