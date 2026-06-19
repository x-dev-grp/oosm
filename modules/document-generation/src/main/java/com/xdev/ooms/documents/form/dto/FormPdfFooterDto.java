package com.xdev.ooms.documents.form.dto;

public class FormPdfFooterDto {
    private String label;
    private String placeholder;

    public FormPdfFooterDto() {
    }

    public FormPdfFooterDto(String label) {
        this.label = label;
    }

    public FormPdfFooterDto(String label, String placeholder) {
        this.label = label;
        this.placeholder = placeholder;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }
}
