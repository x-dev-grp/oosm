package com.xdev.ooms.sharedkernel.communicator.models.shared;


import com.xdev.ooms.sharedkernel.communicator.models.common.dtos.BaseDto;
import com.xdev.ooms.sharedkernel.Enum.ParameterType;

/**
 * DTO for {@link Parameter}
 */
public class ParameterDto extends BaseDto {
    Boolean isActive;
    String code;
    String category;
    String value;
    ParameterType type;
    String description;

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ParameterType getType() {
        return type;
    }

    public void setType(ParameterType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}