package com.xdev.ooms.production.dayimport.service;

import com.xdev.ooms.production.dayimport.model.DayImportWorkbook;
import com.xdev.ooms.production.dayimport.model.DayImportWorkbook.*;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
public class DayImportWorkbookReader {

    public DayImportWorkbook read(InputStream in) throws Exception {
        long start = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(getClass(), "read");
        DayImportWorkbook wb = new DayImportWorkbook();
        try (XSSFWorkbook workbook = new XSSFWorkbook(in)) {
            Map<String, Sheet> sheets = new HashMap<>();
            for (Sheet sheet : workbook) {
                sheets.putIfAbsent(DayImportColumnLabels.canonicalSheet(sheet.getSheetName()), sheet);
            }
            recalculate(workbook);
            wb.setTemplateVersion(propertyVersion(workbook));
            readImportMeta(sheets.get("ImportMeta"), wb);
            readNamed(sheets.get("Regions"), wb.getRegions());
            readNamed(sheets.get("Parcels"), wb.getParcels());
            readNamed(sheets.get("SupplierTypes"), wb.getSupplierTypes());
            readSuppliers(sheets.get("Suppliers"), wb);
            readContainers(sheets.get("OilContainers"), wb);
            readQcRules(sheets.get("QcRules"), wb);
            readReceptions(sheets.get("Receptions"), wb);
            readQc(sheets.get("QcResults"), wb);
            readPayments(sheets.get("Payments"), wb);
            readOilSales(sheets.get("OilSales"), wb);
            readOilSaleContainers(sheets.get("OilSaleContainers"), wb);
            readExpenses(sheets.get("Expenses"), wb);
        }
        OOSMLogger.info(getClass(),
                "[read] businessDate={} regions={} parcels={} suppliers={} receptions={} sales={} expenses={}",
                wb.getBusinessDate(),
                wb.getRegions().size(),
                wb.getParcels().size(),
                wb.getSuppliers().size(),
                wb.getReceptions().size(),
                wb.getOilSales().size(),
                wb.getExpenses().size());
        OOSMLogger.logPerformance(getClass(), "read", start, System.currentTimeMillis());
        return wb;
    }

    private void readImportMeta(Sheet sheet, DayImportWorkbook wb) {
        if (sheet == null) {
            return;
        }
        Map<String, String> map = new HashMap<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            String key = DayImportColumnLabels.key("ImportMetaValues", text(row, 0));
            String value = text(row, 1);
            if (!key.isBlank()) {
                map.put(key.trim().toLowerCase(), value);
            }
        }
        String date = map.getOrDefault("businessdate", "").trim();
        if (!date.isBlank()) {
            wb.setBusinessDate(LocalDate.parse(date));
        }
        String version = map.getOrDefault("templateversion", "").trim();
        if (!version.isBlank()) {
            wb.setTemplateVersion(Integer.parseInt(version));
        }
        String timezone = map.getOrDefault("timezone", "").trim();
        wb.setTimezone(timezone.isBlank() ? "Africa/Tunis" : timezone);
    }

    /** Generated references and lots are formulas; files saved by tools that skip recalculation carry no cached values. */
    private void recalculate(XSSFWorkbook workbook) {
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        for (Sheet sheet : workbook) {
            for (Row row : sheet) {
                for (Cell cell : row) {
                    if (cell.getCellType() != CellType.FORMULA) continue;
                    try {
                        evaluator.evaluateFormulaCell(cell);
                    } catch (RuntimeException ignored) {
                        // keep the value Excel cached
                    }
                }
            }
        }
    }

    /** Current templates keep the version out of the visible sheets; older files carry it in ImportMeta. */
    private int propertyVersion(XSSFWorkbook workbook) {
        var property = workbook.getProperties().getCustomProperties()
                .getProperty(DayImportTemplateFactory.TEMPLATE_VERSION_PROPERTY);
        if (property == null) return 1;
        if (property.isSetI4()) return property.getI4();
        if (property.isSetLpwstr()) return Integer.parseInt(property.getLpwstr().trim());
        return 1;
    }

    private void readNamed(Sheet sheet, java.util.List<NamedRow> target) {
        if (sheet == null) return;
        Map<String, Integer> idx = headerIndex(sheet.getRow(0));
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isEmpty(row)) continue;
            NamedRow n = new NamedRow();
            n.rowNumber = i + 1;
            n.name = cell(row, idx, "name");
            n.description = cell(row, idx, "description");
            if (!blank(n.name)) {
                target.add(n);
            }
        }
    }

    private void readSuppliers(Sheet sheet, DayImportWorkbook wb) {
        if (sheet == null) return;
        Map<String, Integer> idx = headerIndex(sheet.getRow(0));
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isEmpty(row)) continue;
            SupplierRow s = new SupplierRow();
            s.rowNumber = i + 1;
            s.supplierKey = cell(row, idx, "supplierkey");
            s.name = cell(row, idx, "name");
            s.lastname = cell(row, idx, "lastname");
            s.phone = cell(row, idx, "phone");
            s.matriculeFiscal = cell(row, idx, "matriculefiscal");
            s.regionName = cell(row, idx, "regionname");
            s.supplierTypeName = cell(row, idx, "suppliertypename");
            wb.getSuppliers().add(s);
        }
    }

    private void readQcRules(Sheet sheet, DayImportWorkbook wb) {
        if (sheet == null) return;
        Map<String, Integer> idx = headerIndex(sheet.getRow(0));
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isEmpty(row)) continue;
            QcRuleRow r = new QcRuleRow();
            r.rowNumber = i + 1;
            r.ruleKey = cell(row, idx, "rulekey");
            r.ruleName = cell(row, idx, "rulename");
            String oilQc = cell(row, idx, "oilqc");
            r.oilQc = oilQc.isBlank() ? Boolean.TRUE : Boolean.parseBoolean(oilQc);
            r.ruleType = cell(row, idx, "ruletype");
            Double min = dbl(row, idx, "minvalue");
            Double max = dbl(row, idx, "maxvalue");
            r.minValue = min != null ? min.floatValue() : null;
            r.maxValue = max != null ? max.floatValue() : null;
            r.ruleTextValue = cell(row, idx, "ruletextvalue");
            r.description = cell(row, idx, "description");
            wb.getQcRules().add(r);
        }
    }

    private void readContainers(Sheet sheet, DayImportWorkbook wb) {
        if (sheet == null) return;
        Map<String, Integer> idx = headerIndex(sheet.getRow(0));
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isEmpty(row)) continue;
            ContainerRow c = new ContainerRow();
            c.rowNumber = i + 1;
            c.containerKey = cell(row, idx, "containerkey");
            c.name = cell(row, idx, "name");
            c.capacityInLiters = dbl(row, idx, "capacityinliters");
            c.stockQuantity = integer(row, idx, "stockquantity");
            c.buyPrice = dbl(row, idx, "buyprice");
            c.sellingPrice = dbl(row, idx, "sellingprice");
            wb.getContainers().add(c);
        }
    }

    private void readReceptions(Sheet sheet, DayImportWorkbook wb) {
        if (sheet == null) return;
        Map<String, Integer> idx = headerIndex(sheet.getRow(0));
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isEmpty(row)) continue;
            ReceptionRow r = new ReceptionRow();
            r.rowNumber = i + 1;
            r.externalRef = cell(row, idx, "externalref");
            r.deliveryType = cell(row, idx, "deliverytype");
            r.oliveOilType = firstNonBlank(row, idx, "oliveoiltype", "olivetype", "oiltype", "type");
            r.varietyName = firstNonBlank(row, idx, "varietyname", "olivevariety", "oilvariety", "variety");
            r.operationType = cell(row, idx, "operationtype");
            r.supplierKey = cell(row, idx, "supplierkey");
            r.regionName = cell(row, idx, "regionname");
            r.parcelName = cell(row, idx, "parcelname");
            r.poidsNet = dbl(row, idx, "poidsnet");
            r.oilQuantity = dbl(row, idx, "oilquantity");
            r.unitPrice = dbl(row, idx, "unitprice");
            r.storageUnitKey = cell(row, idx, "storageunitkey");
            r.description = cell(row, idx, "description");
            wb.getReceptions().add(r);
        }
    }

    private void readQc(Sheet sheet, DayImportWorkbook wb) {
        if (sheet == null) return;
        Map<String, Integer> idx = headerIndex(sheet.getRow(0));
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isEmpty(row)) continue;
            QcResultRow q = new QcResultRow();
            q.rowNumber = i + 1;
            q.receptionExternalRef = cell(row, idx, "receptionexternalref");
            q.ruleKey = cell(row, idx, "rulekey");
            q.value = cell(row, idx, "value");
            String oilQc = cell(row, idx, "oilqc");
            q.oilQc = oilQc.isBlank() ? null : Boolean.parseBoolean(oilQc);
            wb.getQcResults().add(q);
        }
    }

    private void readPayments(Sheet sheet, DayImportWorkbook wb) {
        if (sheet == null) return;
        Map<String, Integer> idx = headerIndex(sheet.getRow(0));
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isEmpty(row)) continue;
            PaymentRow p = new PaymentRow();
            p.rowNumber = i + 1;
            p.externalRef = cell(row, idx, "externalref");
            String paymentDate = cell(row, idx, "paymentdate");
            p.paymentDate = paymentDate.isBlank() ? wb.getBusinessDate() : LocalDate.parse(paymentDate);
            p.receptionExternalRef = cell(row, idx, "receptionexternalref");
            p.amount = dbl(row, idx, "amount");
            p.paymentMethod = cell(row, idx, "paymentmethod");
            wb.getPayments().add(p);
        }
    }

    private void readOilSales(Sheet sheet, DayImportWorkbook wb) {
        if (sheet == null) return;
        Map<String, Integer> idx = headerIndex(sheet.getRow(0));
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isEmpty(row)) continue;
            OilSaleRow s = new OilSaleRow();
            s.rowNumber = i + 1;
            s.externalRef = cell(row, idx, "externalref");
            s.invoiceNumber = cell(row, idx, "invoicenumber");
            s.supplierKey = cell(row, idx, "supplierkey");
            s.storageUnitKey = cell(row, idx, "storageunitkey");
            s.quantity = dbl(row, idx, "quantity");
            s.unitPrice = dbl(row, idx, "unitprice");
            s.currency = cell(row, idx, "currency");
            s.paymentMethod = cell(row, idx, "paymentmethod");
            s.qualityGrade = cell(row, idx, "qualitygrade");
            s.paidAmount = dbl(row, idx, "paidamount");
            s.description = cell(row, idx, "description");
            wb.getOilSales().add(s);
        }
    }

    private void readOilSaleContainers(Sheet sheet, DayImportWorkbook wb) {
        if (sheet == null) return;
        Map<String, Integer> idx = headerIndex(sheet.getRow(0));
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isEmpty(row)) continue;
            OilSaleContainerRow l = new OilSaleContainerRow();
            l.rowNumber = i + 1;
            l.saleExternalRef = cell(row, idx, "saleexternalref");
            l.containerKey = cell(row, idx, "containerkey");
            l.count = integer(row, idx, "count");
            wb.getOilSaleContainers().add(l);
        }
    }

    private void readExpenses(Sheet sheet, DayImportWorkbook wb) {
        if (sheet == null) return;
        Map<String, Integer> idx = headerIndex(sheet.getRow(0));
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isEmpty(row)) continue;
            ExpenseRow e = new ExpenseRow();
            e.rowNumber = i + 1;
            e.externalRef = cell(row, idx, "externalref");
            e.amount = dbl(row, idx, "amount");
            e.object = cell(row, idx, "object");
            e.purchaseNature = cell(row, idx, "purchasenature");
            e.category = cell(row, idx, "category");
            e.paymentMethod = cell(row, idx, "paymentmethod");
            e.vendor = cell(row, idx, "vendor");
            e.invoiceRef = cell(row, idx, "invoiceref");
            e.notes = cell(row, idx, "notes");
            wb.getExpenses().add(e);
        }
    }

    private Map<String, Integer> headerIndex(Row header) {
        Map<String, Integer> map = new HashMap<>();
        if (header == null) return map;
        for (Iterator<Cell> it = header.cellIterator(); it.hasNext(); ) {
            Cell cell = it.next();
            String name = cell.getStringCellValue();
            if (name != null) {
                map.put(DayImportColumnLabels.key(
                        DayImportColumnLabels.canonicalSheet(header.getSheet().getSheetName()), name), cell.getColumnIndex());
            }
        }
        return map;
    }

    private String cell(Row row, Map<String, Integer> idx, String key) {
        Integer col = idx.get(key);
        if (col == null) return "";
        return text(row, col);
    }

    private String firstNonBlank(Row row, Map<String, Integer> idx, String... keys) {
        for (String key : keys) {
            String value = cell(row, idx, key);
            if (!value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private String text(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date d = cell.getDateCellValue();
                    yield d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
                }
                double v = cell.getNumericCellValue();
                if (Math.rint(v) == v) {
                    yield String.valueOf((long) v);
                }
                yield String.valueOf(v);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> switch (cell.getCachedFormulaResultType()) {
                case STRING -> cell.getStringCellValue().trim();
                case NUMERIC -> {
                    double v = cell.getNumericCellValue();
                    yield Math.rint(v) == v ? String.valueOf((long) v) : String.valueOf(v);
                }
                case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                default -> "";
            };
            default -> "";
        };
    }

    private Double dbl(Row row, Map<String, Integer> idx, String key) {
        String t = cell(row, idx, key);
        if (blank(t)) return null;
        try {
            return Double.parseDouble(t.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(row.getSheet().getSheetName() + " row " + (row.getRowNum() + 1) + ": invalid " + key);
        }
    }

    private Integer integer(Row row, Map<String, Integer> idx, String key) {
        Double d = dbl(row, idx, key);
        if (d != null && (!Double.isFinite(d) || d != Math.rint(d) || d > Integer.MAX_VALUE || d < Integer.MIN_VALUE))
            throw new IllegalArgumentException("Invalid integer " + key + " at row " + (row.getRowNum() + 1));
        return d == null ? null : d.intValue();
    }

    private boolean isEmpty(Row row) {
        if (row == null) return true;
        for (int i = 0; i < Math.max(1, (int) row.getLastCellNum()); i++) {
            if (!text(row, i).isBlank()) return false;
        }
        return true;
    }

    private boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
