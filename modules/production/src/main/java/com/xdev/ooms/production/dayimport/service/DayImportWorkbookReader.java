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
        try (Workbook workbook = new XSSFWorkbook(in)) {
            readImportMeta(workbook.getSheet("ImportMeta"), wb);
            readNamed(workbook.getSheet("Regions"), wb.getRegions());
            readNamed(workbook.getSheet("Parcels"), wb.getParcels());
            readNamed(workbook.getSheet("SupplierTypes"), wb.getSupplierTypes());
            readSuppliers(workbook.getSheet("Suppliers"), wb);
            readContainers(workbook.getSheet("OilContainers"), wb);
            readQcRules(workbook.getSheet("QcRules"), wb);
            readReceptions(workbook.getSheet("Receptions"), wb);
            readQc(workbook.getSheet("QcResults"), wb);
            readPayments(workbook.getSheet("Payments"), wb);
            readOilSales(workbook.getSheet("OilSales"), wb);
            readOilSaleContainers(workbook.getSheet("OilSaleContainers"), wb);
            readExpenses(workbook.getSheet("Expenses"), wb);
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
            String key = text(row, 0);
            String value = text(row, 1);
            if (!key.isBlank()) {
                map.put(key.trim().toLowerCase(), value);
            }
        }
        String date = map.getOrDefault("businessdate", "").trim();
        if (!date.isBlank()) {
            wb.setBusinessDate(LocalDate.parse(date));
        }
        wb.setTimezone(map.getOrDefault("timezone", "Africa/Tunis"));
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
                map.put(name.trim().toLowerCase(), cell.getColumnIndex());
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
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue().trim();
                } catch (Exception e) {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            default -> "";
        };
    }

    private Double dbl(Row row, Map<String, Integer> idx, String key) {
        String t = cell(row, idx, key);
        if (blank(t)) return null;
        try {
            return Double.parseDouble(t.replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer integer(Row row, Map<String, Integer> idx, String key) {
        Double d = dbl(row, idx, key);
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
