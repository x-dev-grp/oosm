package com.xdev.ooms.security.role.dto;

import com.xdev.ooms.security.permission.dto.PermissionDTO;
import com.xdev.ooms.security.role.entity.Role;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.util.Set;

public class RoleDTO extends BaseDto<Role> {
    private String roleName;
    private String description;
    private Set<PermissionDTO> permissions;
    private int usersCount;

    public int getUsersCount() {
        return usersCount;
    }


    public void setUsersCount(int usersCount) {
        this.usersCount = usersCount;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Set<PermissionDTO> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<PermissionDTO> permissions) {
        this.permissions = permissions;
    }
}
