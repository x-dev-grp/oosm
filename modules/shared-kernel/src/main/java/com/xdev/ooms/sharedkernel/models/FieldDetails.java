package com.xdev.ooms.sharedkernel.models;

import java.util.HashMap;
import java.util.Map;

public class FieldDetails {
    private String name;
    private String label;
    private boolean isEnumValue;
    private Map<String, String> enumValues = new HashMap<>();
    private boolean isDynamicColumn;
    private String sourceCollection;

    public FieldDetails() {}

    public FieldDetails(String name, String label) {
        this.name = name;
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isEnumValue() {
        return isEnumValue;
    }

    public void setEnumValue(boolean enumValue) {
        isEnumValue = enumValue;
    }

    public Map<String, String> getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(Map<String, String> enumValues) {
        this.enumValues = enumValues;
    }

    public boolean isDynamicColumn() {
        return isDynamicColumn;
    }

    public void setDynamicColumn(boolean dynamicColumn) {
        isDynamicColumn = dynamicColumn;
    }

    public String getSourceCollection() {
        return sourceCollection;
    }

    public void setSourceCollection(String sourceCollection) {
        this.sourceCollection = sourceCollection;
    }
}
