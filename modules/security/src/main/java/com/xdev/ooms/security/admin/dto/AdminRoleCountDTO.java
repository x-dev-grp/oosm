package com.xdev.ooms.security.admin.dto;

public class AdminRoleCountDTO {
    private String roleName;
    private long count;

    public AdminRoleCountDTO() {
    }

    public AdminRoleCountDTO(String roleName, long count) {
        this.roleName = roleName;
        this.count = count;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
