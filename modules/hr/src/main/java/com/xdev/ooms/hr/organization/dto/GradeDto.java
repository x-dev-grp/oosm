package com.xdev.ooms.hr.organization.dto;

import com.xdev.ooms.hr.organization.entity.Grade;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

public class GradeDto extends BaseDto<Grade> {
    private String code;
    private String name;
    private Integer level;
    private String description;
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

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
