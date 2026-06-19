package com.xdev.ooms.documents.form.dto;

public class FormPdfFieldDto {
    private String label;
    private String value;

    public FormPdfFieldDto() {
    }

    public FormPdfFieldDto(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
