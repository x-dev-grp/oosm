package com.xdev.ooms.production.dayimport.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportRowResultDto {
    private String sheet;
    private int rowNumber;
    private String businessKey;
    private ImportRowStatus status;
    private String message;
    private Double stockDelta;
    private List<ImportFieldErrorDto> fieldErrors = new ArrayList<>();

    public static ImportRowResultDto of(String sheet, int rowNumber, String businessKey,
                                        ImportRowStatus status, String message) {
        ImportRowResultDto dto = new ImportRowResultDto();
        dto.sheet = sheet;
        dto.rowNumber = rowNumber;
        dto.businessKey = businessKey;
        dto.status = status;
        dto.message = message;
        return dto;
    }

    public String getSheet() {
        return sheet;
    }

    public void setSheet(String sheet) {
        this.sheet = sheet;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public ImportRowStatus getStatus() {
        return status;
    }

    public void setStatus(ImportRowStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Double getStockDelta() {
        return stockDelta;
    }

    public void setStockDelta(Double stockDelta) {
        this.stockDelta = stockDelta;
    }

    public List<ImportFieldErrorDto> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(List<ImportFieldErrorDto> fieldErrors) {
        this.fieldErrors = fieldErrors != null ? fieldErrors : new ArrayList<>();
    }
}
