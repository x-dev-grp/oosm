package com.xdev.ooms.production.dayimport.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DayImportReportDto {
    private LocalDate businessDate;
    private boolean canCommit;
    private int validCount;
    private int invalidCount;
    private int totalRows;
    private Map<String, Integer> statusCounts = new LinkedHashMap<>();
    private double stockInTotal;
    private double stockOutTotal;
    private double expenseTotal;
    private List<ImportRowResultDto> rows = new ArrayList<>();

    public void addRow(ImportRowResultDto row) {
        rows.add(row);
        totalRows++;
        if (row.getStatus() == ImportRowStatus.ERROR) {
            invalidCount++;
        } else {
            validCount++;
        }
        statusCounts.merge(row.getStatus().name(), 1, Integer::sum);
        if (row.getStockDelta() != null) {
            if (row.getStockDelta() > 0) {
                stockInTotal += row.getStockDelta();
            } else if (row.getStockDelta() < 0) {
                stockOutTotal += Math.abs(row.getStockDelta());
            }
        }
    }

    public void finalizeReport() {
        canCommit = invalidCount == 0 && businessDate != null;
    }

    public LocalDate getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate(LocalDate businessDate) {
        this.businessDate = businessDate;
    }

    public boolean isCanCommit() {
        return canCommit;
    }

    public void setCanCommit(boolean canCommit) {
        this.canCommit = canCommit;
    }

    public int getValidCount() {
        return validCount;
    }

    public void setValidCount(int validCount) {
        this.validCount = validCount;
    }

    public int getInvalidCount() {
        return invalidCount;
    }

    public void setInvalidCount(int invalidCount) {
        this.invalidCount = invalidCount;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public Map<String, Integer> getStatusCounts() {
        return statusCounts;
    }

    public void setStatusCounts(Map<String, Integer> statusCounts) {
        this.statusCounts = statusCounts;
    }

    public double getStockInTotal() {
        return stockInTotal;
    }

    public void setStockInTotal(double stockInTotal) {
        this.stockInTotal = stockInTotal;
    }

    public double getStockOutTotal() {
        return stockOutTotal;
    }

    public void setStockOutTotal(double stockOutTotal) {
        this.stockOutTotal = stockOutTotal;
    }

    public double getExpenseTotal() {
        return expenseTotal;
    }

    public void setExpenseTotal(double expenseTotal) {
        this.expenseTotal = expenseTotal;
    }

    public List<ImportRowResultDto> getRows() {
        return rows;
    }

    public void setRows(List<ImportRowResultDto> rows) {
        this.rows = rows;
    }
}
