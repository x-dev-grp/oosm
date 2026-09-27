package com.xdev.ooms.production.dayimport.service;

import com.xdev.ooms.production.dayimport.dto.DayImportReportDto;

public class DayImportRejectedException extends RuntimeException {
    private final DayImportReportDto report;
    public DayImportRejectedException(String message, DayImportReportDto report) {
        super(message);
        this.report = report;
    }
    public DayImportReportDto getReport() { return report; }
}
