package com.xdev.ooms.production.dayimport.service;

import com.xdev.ooms.production.dayimport.dto.*;
import com.xdev.ooms.production.dayimport.model.DayImportWorkbook;
import com.xdev.ooms.sharedkernel.Enum.*;
import java.util.*;
import java.util.function.Function;

/** Pure structural validation happens before any create-if-missing operation. */
final class DayImportValidation {
    private DayImportValidation() {}
    static void validate(DayImportWorkbook wb, DayImportReportDto out) {
        if (wb.getTemplateVersion() != 1 && wb.getTemplateVersion() != 2)
            error(out, "ImportMeta", 1, "templateVersion", "Unsupported template version");
        unique(wb.getReceptions(), r -> r.externalRef, "Receptions", out);
        unique(wb.getOilSales(), r -> blank(r.invoiceNumber) ? r.externalRef : r.invoiceNumber, "OilSales", out);
        unique(wb.getExpenses(), r -> r.externalRef, "Expenses", out);
        unique(wb.getPayments(), r -> r.externalRef, "Payments", out);
        unique(wb.getSuppliers(), r -> r.supplierKey, "Suppliers", out);
        unique(wb.getContainers(), r -> blank(r.name) ? r.containerKey : r.name, "OilContainers", out);
        unique(wb.getRegions(), r -> r.name, "Regions", out);
        unique(wb.getParcels(), r -> r.name, "Parcels", out);
        unique(wb.getSupplierTypes(), r -> r.name, "SupplierTypes", out);
        unique(wb.getQcRules(), r -> r.ruleKey + "|" + (r.oilQc == null || r.oilQc), "QcRules", out);
        unique(wb.getQcResults(), r -> r.receptionExternalRef + "|" + r.ruleKey + "|" + r.oilQc, "QcResults", out);
        for (var r : wb.getReceptions()) {
            numeric(r, "Receptions", out);
        }
        for (var r : wb.getPayments()) {
            numeric(r, "Payments", out);
            if (r.paymentDate != null && r.paymentDate.isAfter(java.time.LocalDate.now()))
                error(out, "Payments", r.rowNumber, "paymentDate", "Payment date cannot be in the future");
        }
        for (var r : wb.getOilSales()) {
            numeric(r, "OilSales", out);
            enumValue(r.paymentMethod, PaymentMethod.class, "OilSales", r.rowNumber, "paymentMethod", out);
            enumValue(r.currency, com.xdev.ooms.sharedkernel.Enum.Currency.class, "OilSales", r.rowNumber, "currency", out);
            enumValue(r.qualityGrade, QualityGrades.class, "OilSales", r.rowNumber, "qualityGrade", out);
            if (r.quantity != null && r.quantity > 0 && (r.unitPrice == null || r.unitPrice <= 0))
                error(out, "OilSales", r.rowNumber, "unitPrice", "Positive unit price required");
        }
        for (var r : wb.getOilSaleContainers()) {
            boolean found = wb.getOilSales().stream().anyMatch(s -> !blank(s.externalRef) && DayImportLedger.key(s.externalRef).equals(DayImportLedger.key(r.saleExternalRef)));
            if (!found) error(out, "OilSaleContainers", r.rowNumber, r.saleExternalRef, "Sale reference not found in workbook");
        }
        for (var r : wb.getExpenses()) {
            numeric(r, "Expenses", out);
            enumValue(r.paymentMethod, PaymentMethod.class, "Expenses", r.rowNumber, "paymentMethod", out);
            enumValue(r.category, ExpenseCategory.class, "Expenses", r.rowNumber, "category", out);
        }
        for (var r : wb.getContainers()) numeric(r, "OilContainers", out);
    }
    private static <T> void unique(List<T> rows, Function<T,String> key, String sheet, DayImportReportDto out) {
        Set<String> seen = new HashSet<>();
        for (T row : rows) {
            String ref = key.apply(row);
            if (!blank(ref) && !seen.add(DayImportLedger.key(ref))) error(out, sheet, rowNumber(row), ref, "Duplicate key in workbook");
            if (ref != null && ref.length() > 255) error(out, sheet, rowNumber(row), "externalRef", "Reference exceeds 255 characters");
        }
    }
    private static void numeric(Object row, String sheet, DayImportReportDto out) {
        for (var field : row.getClass().getFields()) {
            try {
                if (!field.getName().equals("rowNumber") && field.get(row) instanceof Number value && (!Double.isFinite(value.doubleValue()) || value.doubleValue() < 0))
                    error(out, sheet, rowNumber(row), field.getName(), "Finite nonnegative number required");
            } catch (IllegalAccessException e) { throw new IllegalStateException(e); }
        }
    }
    private static int rowNumber(Object row) {
        try { return row.getClass().getField("rowNumber").getInt(row); } catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
    }
    private static <T extends Enum<T>> void enumValue(String value, Class<T> type, String sheet, int row, String field, DayImportReportDto out) {
        if (blank(value)) return;
        try { Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException e) { error(out, sheet, row, field, "Unknown " + field + ": " + value); }
    }
    private static boolean blank(String s) { return s == null || s.isBlank(); }
    private static void error(DayImportReportDto out, String sheet, int row, String key, String message) {
        out.addRow(ImportRowResultDto.of(sheet, row, key, ImportRowStatus.ERROR, message));
    }
}
