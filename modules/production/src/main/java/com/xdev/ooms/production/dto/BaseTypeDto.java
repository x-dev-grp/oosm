package com.xdev.ooms.production.dto;

import com.xdev.ooms.sharedkernel.Enum.TypeCategory;
import com.xdev.ooms.production.model.BaseType;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;


public class BaseTypeDto extends BaseDto<BaseType> {
    private String name; // The name of the type (e.g., "Plastic Waste", "Local SupplierInfo")
    private String description; // Description of the type

    private TypeCategory type;

    public BaseTypeDto() {
    }


    public TypeCategory getType() {
        return type;
    }

    public void setType(TypeCategory type) {
        this.type = type;
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


}