package com.xdev.ooms.hr.organization.dto;

import com.xdev.ooms.hr.organization.entity.Department;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.util.UUID;

public class DepartmentDto extends BaseDto<Department> {
    private String code;
    private String name;
    private String description;
    private DepartmentDto parentDepartment;
    private UUID managerEmployeeId;
    private Boolean active;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DepartmentDto getParentDepartment() {
        return parentDepartment;
    }

    public void setParentDepartment(DepartmentDto parentDepartment) {
        this.parentDepartment = parentDepartment;
    }

    public UUID getManagerEmployeeId() {
        return managerEmployeeId;
    }

    public void setManagerEmployeeId(UUID managerEmployeeId) {
        this.managerEmployeeId = managerEmployeeId;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
