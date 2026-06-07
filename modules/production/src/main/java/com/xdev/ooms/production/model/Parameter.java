package com.xdev.ooms.production.model;

import  com.xdev.ooms.sharedkernel.Enum.ParameterType;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.io.Serializable;
@Entity
public class Parameter extends BaseEntity implements Serializable {

    private final Boolean isActive = true;
    private String code; // e.g. "OLIVE_UNIT_PRICE"
    private String category; // e.g. "PRICING", "SYSTEM"
    private String value;
    @Enumerated(EnumType.STRING)
    private ParameterType type;
    private String description;

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

    public Boolean getActive() {
        return isActive;
    }

}
