package com.xdev.ooms.sharedkernel.communicator.models.common.dtos.apiDTOs.models;

import java.util.List;

public class ExportDetails {
    private SearchData searchData;
    private List<FieldDetails> fieldDetails;
    private String fileName;

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
