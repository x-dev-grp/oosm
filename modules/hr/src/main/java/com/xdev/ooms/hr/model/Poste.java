package com.xdev.ooms.hr.model;

import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity
public class Poste extends BaseEntity implements Serializable {
    private String name;
    private String description;

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

