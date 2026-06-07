package com.xdev.ooms.sharedkernel.models;

public class CollectionFieldDetails {
    private String collectionPath;
    private String nameField;
    private String valueField;
    private String columnPrefix;


    public CollectionFieldDetails() {}

    public CollectionFieldDetails(String collectionPath, String nameField, String valueField) {
        this.collectionPath = collectionPath;
        this.nameField = nameField;
        this.valueField = valueField;
    }

    public String getCollectionPath() {
        return collectionPath;
    }

    public void setCollectionPath(String collectionPath) {
        this.collectionPath = collectionPath;
    }

    public String getNameField() {
        return nameField;
    }

    public void setNameField(String nameField) {
        this.nameField = nameField;
    }

    public String getValueField() {
        return valueField;
    }

    public void setValueField(String valueField) {
        this.valueField = valueField;
    }

    public String getColumnPrefix() {
        return columnPrefix;
    }

    public void setColumnPrefix(String columnPrefix) {
        this.columnPrefix = columnPrefix;
    }
}
