package com.xdev.ooms.sharedkernel.communicator.models.shared;


import com.xdev.ooms.sharedkernel.communicator.models.common.dtos.apiDTOs.models.OSMModule;

public class PermissionDTO{
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
