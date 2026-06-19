package com.xdev.ooms.documents.form.dto;

import java.util.ArrayList;
import java.util.List;

public class FormPdfConfigDto {
    private String title;
    private String reference;
    private String number;
    private String revision;
    private String date;
    private List<FormPdfFieldDto> generalInfo = new ArrayList<>();
    private List<FormPdfFieldDto> fields = new ArrayList<>();
    private List<FormPdfFooterDto> footerInfo = new ArrayList<>();
    private String fileName;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<FormPdfFieldDto> getGeneralInfo() {
        return generalInfo;
    }

    public void setGeneralInfo(List<FormPdfFieldDto> generalInfo) {
        this.generalInfo = generalInfo == null ? new ArrayList<>() : generalInfo;
    }

    public List<FormPdfFieldDto> getFields() {
        return fields;
    }

    public void setFields(List<FormPdfFieldDto> fields) {
        this.fields = fields == null ? new ArrayList<>() : fields;
    }

    public List<FormPdfFooterDto> getFooterInfo() {
        return footerInfo;
    }

    public void setFooterInfo(List<FormPdfFooterDto> footerInfo) {
        this.footerInfo = footerInfo == null ? new ArrayList<>() : footerInfo;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
