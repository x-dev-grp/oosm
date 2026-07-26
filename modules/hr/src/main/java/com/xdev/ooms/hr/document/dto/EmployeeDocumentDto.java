package com.xdev.ooms.hr.document.dto;

import com.xdev.ooms.hr.common.enums.EmployeeDocumentType;
import com.xdev.ooms.hr.document.entity.EmployeeDocument;
import com.xdev.ooms.hr.employee.dto.EmployeeDto;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.time.LocalDate;

public class EmployeeDocumentDto extends BaseDto<EmployeeDocument> {
    private EmployeeDto employee;
    private EmployeeDocumentType documentType;
    private String title;
    private String fileName;
    private String contentType;
    private String storageRef;
    private LocalDate expiryDate;
    private String status;
    private String notes;

    public EmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeDto employee) {
        this.employee = employee;
    }

    public EmployeeDocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(EmployeeDocumentType documentType) {
        this.documentType = documentType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getStorageRef() {
        return storageRef;
    }

    public void setStorageRef(String storageRef) {
        this.storageRef = storageRef;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
