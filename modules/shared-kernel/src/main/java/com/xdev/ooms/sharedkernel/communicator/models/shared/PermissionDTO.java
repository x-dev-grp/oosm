package com.xdev.ooms.sharedkernel.communicator.models.shared;


import com.xdev.ooms.sharedkernel.communicator.models.common.dtos.apiDTOs.models.OOSMModule;

public class PermissionDTO{
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
