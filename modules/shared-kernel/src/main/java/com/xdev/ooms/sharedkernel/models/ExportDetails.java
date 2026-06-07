package com.xdev.ooms.sharedkernel.models;

import java.util.List;

public class ExportDetails {
    private SearchData searchData;
    private List<FieldDetails> fieldDetails;
    private String fileName;
    private List<CollectionFieldDetails> collectionFields;

    public List<CollectionFieldDetails> getCollectionFields() {
        return collectionFields;
    }

    public void setCollectionFields(List<CollectionFieldDetails> collectionFields) {
        this.collectionFields = collectionFields;
    }

    public SearchData getSearchData() {
        return searchData;
    }

    public void setSearchData(SearchData searchData) {
        this.searchData = searchData;
    }

    public List<FieldDetails> getFieldDetails() {
        return fieldDetails;
    }

    public void setFieldDetails(List<FieldDetails> fieldDetails) {
        this.fieldDetails = fieldDetails;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
