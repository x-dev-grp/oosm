package com.xdev.ooms.production.dayimport.service;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFName;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;

/**
 * Builds blank / sample day-import workbooks.
 * Sheet names + headers stay aligned with {@link DayImportWorkbookReader}.
 * Styling matches OOSM (primary #4680FF + olive production accent).
 */
@Component
public class DayImportTemplateFactory {

    private static final int LIST_ROWS = 200;

    public byte[] blankTemplate() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            buildWorkbook(wb, null, false);
            wb.write(out);
            return out.toByteArray();
        }
    }

    public byte[] sampleTemplate(LocalDate businessDate) throws IOException {
        LocalDate day = businessDate != null ? businessDate : LocalDate.now();
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            buildWorkbook(wb, day, true);
            wb.write(out);
            return out.toByteArray();
        }
    }

    private void buildWorkbook(XSSFWorkbook wb, LocalDate businessDate, boolean sample) {
        Styles styles = new Styles(wb);

        writeGuide(wb, styles, businessDate, sample);
        writeImportMeta(wb, styles, businessDate);

        Sheet regions = createSheet(wb, "Regions");
        writeHeaders(regions, styles, "name", "description");

        Sheet parcels = createSheet(wb, "Parcels");
        writeHeaders(parcels, styles, "name", "description");

        Sheet supplierTypes = createSheet(wb, "SupplierTypes");
        writeHeaders(supplierTypes, styles, "name", "description");

        Sheet qcRules = createSheet(wb, "QcRules");
        writeHeaders(qcRules, styles,
                "ruleKey", "ruleName", "oilQc", "ruleType", "minValue", "maxValue", "ruleTextValue", "description");

        Sheet suppliers = createSheet(wb, "Suppliers");
        writeHeaders(suppliers, styles,
                "supplierKey", "name", "lastname", "phone", "matriculeFiscal", "regionName", "supplierTypeName");

        Sheet containers = createSheet(wb, "OilContainers");
        writeHeaders(containers, styles,
                "containerKey", "name", "capacityInLiters", "stockQuantity", "buyPrice", "sellingPrice");

        // Dropdown source only — tanks must already exist in the app.
        Sheet storageUnits = createSheet(wb, "StorageUnits");
        writeHeaders(storageUnits, styles, "storageUnitKey", "name");

        Sheet receptions = createSheet(wb, "Receptions");
        writeHeaders(receptions, styles,
                "externalRef", "deliveryType", "oliveOilType", "varietyName", "operationType",
                "supplierKey", "regionName", "parcelName", "poidsNet", "oilQuantity", "unitPrice",
                "storageUnitKey", "description");

        Sheet qcResults = createSheet(wb, "QcResults");
        writeHeaders(qcResults, styles, "receptionExternalRef", "ruleKey", "value", "oilQc");

        Sheet payments = createSheet(wb, "Payments");
        writeHeaders(payments, styles, "receptionExternalRef", "amount", "paymentMethod");

        Sheet oilSales = createSheet(wb, "OilSales");
        writeHeaders(oilSales, styles,
                "externalRef", "invoiceNumber", "supplierKey", "storageUnitKey", "quantity", "unitPrice",
                "currency", "paymentMethod", "qualityGrade", "paidAmount", "description");

        Sheet oilSaleContainers = createSheet(wb, "OilSaleContainers");
        writeHeaders(oilSaleContainers, styles, "saleExternalRef", "containerKey", "count");

        Sheet expenses = createSheet(wb, "Expenses");
        writeHeaders(expenses, styles,
                "externalRef", "amount", "object", "purchaseNature", "category", "paymentMethod",
                "vendor", "invoiceRef", "notes");

        if (sample) {
            fillSample(regions, parcels, supplierTypes, qcRules, suppliers, containers, storageUnits,
                    receptions, qcResults, payments, oilSales, oilSaleContainers, expenses, styles);
        }

        createNamedRange(wb, "SupplierKeys", "Suppliers!$A$2:$A$" + (LIST_ROWS + 1));
        createNamedRange(wb, "RegionNames", "Regions!$A$2:$A$" + (LIST_ROWS + 1));
        createNamedRange(wb, "ParcelNames", "Parcels!$A$2:$A$" + (LIST_ROWS + 1));
        createNamedRange(wb, "SupplierTypeNames", "SupplierTypes!$A$2:$A$" + (LIST_ROWS + 1));
        createNamedRange(wb, "ContainerKeys", "OilContainers!$A$2:$A$" + (LIST_ROWS + 1));
        createNamedRange(wb, "StorageUnitKeys", "StorageUnits!$A$2:$A$" + (LIST_ROWS + 1));
        createNamedRange(wb, "ReceptionRefs", "Receptions!$A$2:$A$" + (LIST_ROWS + 1));
        createNamedRange(wb, "SaleRefs", "OilSales!$A$2:$A$" + (LIST_ROWS + 1));
        createNamedRange(wb, "QcRuleKeys", "QcRules!$A$2:$A$" + (LIST_ROWS + 1));

        addListValidation(suppliers, 1, LIST_ROWS, 5, "RegionNames");
        addListValidation(suppliers, 1, LIST_ROWS, 6, "SupplierTypeNames");

        addExplicitList(receptions, 1, LIST_ROWS, 1, "OLIVE,OIL");
        addExplicitList(receptions, 1, LIST_ROWS, 2, "OC,OB,HC,HB");
        addExplicitList(receptions, 1, LIST_ROWS, 4,
                "SIMPLE_RECEPTION,OLIVE_PURCHASE,OIL_PURCHASE,EXCHANGE,BASE");
        addListValidation(receptions, 1, LIST_ROWS, 5, "SupplierKeys");
        addListValidation(receptions, 1, LIST_ROWS, 6, "RegionNames");
        addListValidation(receptions, 1, LIST_ROWS, 7, "ParcelNames");
        addListValidation(receptions, 1, LIST_ROWS, 11, "StorageUnitKeys");

        addListValidation(qcResults, 1, LIST_ROWS, 0, "ReceptionRefs");
        addListValidation(qcResults, 1, LIST_ROWS, 1, "QcRuleKeys");
        addExplicitList(qcResults, 1, LIST_ROWS, 3, "true,false");

        addListValidation(payments, 1, LIST_ROWS, 0, "ReceptionRefs");
        addExplicitList(payments, 1, LIST_ROWS, 2, "CASH,CHECK,TRANSFER,CARD");

        addListValidation(oilSales, 1, LIST_ROWS, 2, "SupplierKeys");
        addListValidation(oilSales, 1, LIST_ROWS, 3, "StorageUnitKeys");
        addExplicitList(oilSales, 1, LIST_ROWS, 6, "TND,EUR,USD");
        addExplicitList(oilSales, 1, LIST_ROWS, 7, "CASH,CHECK,TRANSFER,CARD");
        addExplicitList(oilSales, 1, LIST_ROWS, 8, "EXTRA_VIRGIN,VIRGIN,LAMPANTE,REFINED,POMACE");

        addListValidation(oilSaleContainers, 1, LIST_ROWS, 0, "SaleRefs");
        addListValidation(oilSaleContainers, 1, LIST_ROWS, 1, "ContainerKeys");

        addExplicitList(expenses, 1, LIST_ROWS, 5, "CASH,CHECK,TRANSFER,CARD");
        addExplicitList(qcRules, 1, LIST_ROWS, 2, "true,false");
        addExplicitList(qcRules, 1, LIST_ROWS, 3, "NUMERIC,STRING,BOOLEAN");

        wb.setActiveSheet(0);
    }

    private void writeGuide(XSSFWorkbook wb, Styles styles, LocalDate businessDate, boolean sample) {
        Sheet guide = wb.createSheet("Guide");
        guide.setColumnWidth(0, 22 * 256);
        guide.setColumnWidth(1, 96 * 256);

        Row title = guide.createRow(0);
        title.setHeightInPoints(28);
        Cell titleCell = title.createCell(0);
        titleCell.setCellValue("OOSM — Day import workbook");
        titleCell.setCellStyle(styles.title);
        guide.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

        Row subtitle = guide.createRow(1);
        subtitle.setHeightInPoints(20);
        Cell sub = subtitle.createCell(0);
        sub.setCellValue("Aligned with Abioc / OOSM reception · olive & oil operations");
        sub.setCellStyle(styles.subtitle);
        guide.addMergedRegion(new CellRangeAddress(1, 1, 0, 1));

        int r = 3;
        r = guidePair(guide, styles, r, "Workflow",
                "1) Fill masters  2) Receptions (oliveOilType + varietyName)  3) Dry-run in app  4) Commit");
        r = guidePair(guide, styles, r, "Lot number",
                "Receptions.oliveOilType (OC|OB, HC|HB accepted) → lot like 0008OC26 = seq + type + year");
        r = guidePair(guide, styles, r, "Variety",
                "Receptions.varietyName → generic OLIVE_VARIETY or OIL_VARIETY (create-if-missing), e.g. Chemlali");
        r = guidePair(guide, styles, r, "Idempotency",
                "Reception stamp [IMP:externalRef]. Oil sale invoice or [IMP:sale:externalRef]. Duplicates are skipped.");
        r = guidePair(guide, styles, r, "Tanks",
                "StorageUnits feeds dropdowns only. Create tanks in the app first (import does not create them).");
        r = guidePair(guide, styles, r, "Masters",
                "Regions / Parcels / SupplierTypes / QcRules / Suppliers / OilContainers: link existing or create-if-missing.");
        r = guidePair(guide, styles, r, "Operations",
                "OLIVE: SIMPLE_RECEPTION, OLIVE_PURCHASE, EXCHANGE, BASE · OIL: OIL_PURCHASE");
        r = guidePair(guide, styles, r, "Business date",
                "ImportMeta.businessDate (yyyy-MM-dd) — one file = one day");
        if (sample) {
            guidePair(guide, styles, r, "Sample",
                    "Demo rows included for " + (businessDate != null ? businessDate : LocalDate.now()));
        }
    }

    private int guidePair(Sheet sheet, Styles styles, int rowIdx, String label, String value) {
        Row row = sheet.createRow(rowIdx);
        row.setHeightInPoints(20);
        Cell a = row.createCell(0);
        a.setCellValue(label);
        a.setCellStyle(styles.guideLabel);
        Cell b = row.createCell(1);
        b.setCellValue(value);
        b.setCellStyle(styles.guideValue);
        return rowIdx + 1;
    }

    private void writeImportMeta(XSSFWorkbook wb, Styles styles, LocalDate businessDate) {
        Sheet meta = createSheet(wb, "ImportMeta");
        writeHeaders(meta, styles, "key", "value");
        set(meta.getRow(1), 0, "businessDate", styles.data);
        set(meta.getRow(1), 1, businessDate != null ? businessDate.toString() : "", styles.data);
        set(meta.getRow(2), 0, "timezone", styles.data);
        set(meta.getRow(2), 1, "Africa/Tunis", styles.data);
    }

    private void fillSample(Sheet regions, Sheet parcels, Sheet supplierTypes, Sheet qcRules, Sheet suppliers,
                            Sheet containers, Sheet storageUnits, Sheet receptions, Sheet qcResults,
                            Sheet payments, Sheet oilSales, Sheet oilSaleContainers, Sheet expenses,
                            Styles styles) {
        sample(regions, styles, 1, "SampleRegion", "Demo region");
        sample(parcels, styles, 1, "SampleParcel", "Demo parcel");
        sample(supplierTypes, styles, 1, "Apporteur", "Default");

        Row rule = qcRules.getRow(1);
        set(rule, 0, "Acidite", styles.data);
        set(rule, 1, "Acidité", styles.data);
        set(rule, 2, "true", styles.data);
        set(rule, 3, "NUMERIC", styles.data);
        set(rule, 4, "0", styles.data);
        set(rule, 5, "3.3", styles.data);
        set(rule, 6, "", styles.data);
        set(rule, 7, "Oil acidity", styles.data);

        Row s = suppliers.getRow(1);
        set(s, 0, "SUP-001", styles.data);
        set(s, 1, "Ali", styles.data);
        set(s, 2, "Ben", styles.data);
        set(s, 3, "20000000", styles.data);
        set(s, 4, "", styles.data);
        set(s, 5, "SampleRegion", styles.data);
        set(s, 6, "Apporteur", styles.data);

        Row c = containers.getRow(1);
        set(c, 0, "BIN-5L", styles.data);
        set(c, 1, "Bidon 5L", styles.data);
        set(c, 2, "5", styles.data);
        set(c, 3, "100", styles.data);
        set(c, 4, "2", styles.data);
        set(c, 5, "5", styles.data);

        sample(storageUnits, styles, 1, "Cuve-1", "Cuve-1");

        Row rOlive = receptions.getRow(1);
        set(rOlive, 0, "R-OLIVE-001", styles.data);
        set(rOlive, 1, "OLIVE", styles.data);
        set(rOlive, 2, "OC", styles.data);
        set(rOlive, 3, "Chemlali", styles.data);
        set(rOlive, 4, "SIMPLE_RECEPTION", styles.data);
        set(rOlive, 5, "SUP-001", styles.data);
        set(rOlive, 6, "SampleRegion", styles.data);
        set(rOlive, 7, "SampleParcel", styles.data);
        set(rOlive, 8, "3200", styles.data);
        set(rOlive, 9, "480", styles.data);
        set(rOlive, 10, "", styles.data);
        set(rOlive, 11, "", styles.data);
        set(rOlive, 12, "Sample olive milling reception", styles.data);

        Row rOil = receptions.getRow(2);
        set(rOil, 0, "R-OIL-001", styles.data);
        set(rOil, 1, "OIL", styles.data);
        set(rOil, 2, "OC", styles.data);
        set(rOil, 3, "Chemlali oil", styles.data);
        set(rOil, 4, "OIL_PURCHASE", styles.data);
        set(rOil, 5, "SUP-001", styles.data);
        set(rOil, 6, "SampleRegion", styles.data);
        set(rOil, 7, "SampleParcel", styles.data);
        set(rOil, 8, "", styles.data);
        set(rOil, 9, "150", styles.data);
        set(rOil, 10, "12", styles.data);
        set(rOil, 11, "Cuve-1", styles.data);
        set(rOil, 12, "Sample oil purchase into tank", styles.data);

        Row qr = qcResults.getRow(1);
        set(qr, 0, "R-OIL-001", styles.data);
        set(qr, 1, "Acidite", styles.data);
        set(qr, 2, "0.4", styles.data);
        set(qr, 3, "true", styles.data);

        Row p = payments.getRow(1);
        set(p, 0, "R-OIL-001", styles.data);
        set(p, 1, "500", styles.data);
        set(p, 2, "CASH", styles.data);

        Row sale = oilSales.getRow(1);
        set(sale, 0, "S-001", styles.data);
        set(sale, 1, "INV-DEMO-001", styles.data);
        set(sale, 2, "SUP-001", styles.data);
        set(sale, 3, "Cuve-1", styles.data);
        set(sale, 4, "20", styles.data);
        set(sale, 5, "15", styles.data);
        set(sale, 6, "TND", styles.data);
        set(sale, 7, "CASH", styles.data);
        set(sale, 8, "EXTRA_VIRGIN", styles.data);
        set(sale, 9, "300", styles.data);
        set(sale, 10, "Sample sale", styles.data);

        Row line = oilSaleContainers.getRow(1);
        set(line, 0, "S-001", styles.data);
        set(line, 1, "BIN-5L", styles.data);
        set(line, 2, "2", styles.data);

        Row e = expenses.getRow(1);
        set(e, 0, "EXP-001", styles.data);
        set(e, 1, "50", styles.data);
        set(e, 2, "Fuel", styles.data);
        set(e, 3, "Diesel", styles.data);
        set(e, 4, "OTHER", styles.data);
        set(e, 5, "CASH", styles.data);
        set(e, 6, "Station", styles.data);
        set(e, 7, "EXP-INV-1", styles.data);
        set(e, 8, "", styles.data);
    }

    private Sheet createSheet(XSSFWorkbook wb, String name) {
        Sheet sheet = wb.createSheet(name);
        sheet.createFreezePane(0, 1);
        sheet.setDefaultRowHeightInPoints(18);
        return sheet;
    }

    private void writeHeaders(Sheet sheet, Styles styles, String... headers) {
        Row header = sheet.createRow(0);
        header.setHeightInPoints(22);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.header);
            sheet.setColumnWidth(i, Math.min(30, Math.max(14, headers[i].length() + 4)) * 256);
        }
        for (int r = 1; r <= 40; r++) {
            Row data = sheet.createRow(r);
            for (int c = 0; c < headers.length; c++) {
                Cell cell = data.createCell(c);
                cell.setCellStyle(r % 2 == 0 ? styles.alt : styles.data);
            }
        }
    }

    private void sample(Sheet sheet, Styles styles, int rowIdx, String... values) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) {
            row = sheet.createRow(rowIdx);
        }
        for (int i = 0; i < values.length; i++) {
            set(row, i, values[i], styles.data);
        }
    }

    private void set(Row row, int col, String value, CellStyle style) {
        Cell cell = row.getCell(col);
        if (cell == null) {
            cell = row.createCell(col);
        }
        cell.setCellValue(value != null ? value : "");
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    private void createNamedRange(XSSFWorkbook wb, String name, String refersTo) {
        XSSFName named = wb.createName();
        named.setNameName(name);
        named.setRefersToFormula(refersTo);
    }

    private void addListValidation(Sheet sheet, int firstRow, int lastRow, int col, String namedRange) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createFormulaListConstraint(namedRange);
        CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, col, col);
        DataValidation validation = helper.createValidation(constraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        validation.setErrorStyle(DataValidation.ErrorStyle.WARNING);
        sheet.addValidationData(validation);
    }

    private void addExplicitList(Sheet sheet, int firstRow, int lastRow, int col, String csv) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createExplicitListConstraint(csv.split(","));
        CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, col, col);
        DataValidation validation = helper.createValidation(constraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }

    private static final class Styles {
        private static final byte[] PRIMARY = new byte[]{(byte) 0x46, (byte) 0x80, (byte) 0xFF};
        private static final byte[] OLIVE = new byte[]{(byte) 0x3D, (byte) 0x5A, (byte) 0x2C};
        private static final byte[] GREY_100 = new byte[]{(byte) 0xF8, (byte) 0xF9, (byte) 0xFA};
        private static final byte[] GREY_200 = new byte[]{(byte) 0xF3, (byte) 0xF5, (byte) 0xF7};
        private static final byte[] GREY_BORDER = new byte[]{(byte) 0xDB, (byte) 0xE0, (byte) 0xE5};
        private static final byte[] WHITE = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        private static final byte[] TEXT = new byte[]{(byte) 0x3E, (byte) 0x48, (byte) 0x53};

        final XSSFCellStyle title;
        final XSSFCellStyle subtitle;
        final XSSFCellStyle header;
        final XSSFCellStyle data;
        final XSSFCellStyle alt;
        final XSSFCellStyle guideLabel;
        final XSSFCellStyle guideValue;

        Styles(XSSFWorkbook wb) {
            DefaultIndexedColorMap colors = new DefaultIndexedColorMap();

            XSSFFont titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setColor(new XSSFColor(WHITE, colors));

            XSSFFont subtitleFont = wb.createFont();
            subtitleFont.setFontHeightInPoints((short) 11);
            subtitleFont.setColor(new XSSFColor(TEXT, colors));

            XSSFFont headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setColor(new XSSFColor(WHITE, colors));

            XSSFFont labelFont = wb.createFont();
            labelFont.setBold(true);
            labelFont.setFontHeightInPoints((short) 11);
            labelFont.setColor(new XSSFColor(OLIVE, colors));

            XSSFFont bodyFont = wb.createFont();
            bodyFont.setFontHeightInPoints((short) 10);
            bodyFont.setColor(new XSSFColor(TEXT, colors));

            title = wb.createCellStyle();
            title.setFont(titleFont);
            title.setFillForegroundColor(new XSSFColor(OLIVE, colors));
            title.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            title.setVerticalAlignment(VerticalAlignment.CENTER);
            title.setAlignment(HorizontalAlignment.LEFT);
            border(title, colors);

            subtitle = wb.createCellStyle();
            subtitle.setFont(subtitleFont);
            subtitle.setFillForegroundColor(new XSSFColor(GREY_100, colors));
            subtitle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            subtitle.setVerticalAlignment(VerticalAlignment.CENTER);

            header = wb.createCellStyle();
            header.setFont(headerFont);
            header.setFillForegroundColor(new XSSFColor(PRIMARY, colors));
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            header.setAlignment(HorizontalAlignment.CENTER);
            header.setVerticalAlignment(VerticalAlignment.CENTER);
            header.setWrapText(true);
            border(header, colors);

            data = wb.createCellStyle();
            data.setFont(bodyFont);
            data.setVerticalAlignment(VerticalAlignment.CENTER);
            border(data, colors);

            alt = wb.createCellStyle();
            alt.cloneStyleFrom(data);
            alt.setFillForegroundColor(new XSSFColor(GREY_200, colors));
            alt.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            guideLabel = wb.createCellStyle();
            guideLabel.setFont(labelFont);
            guideLabel.setFillForegroundColor(new XSSFColor(GREY_100, colors));
            guideLabel.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            guideLabel.setVerticalAlignment(VerticalAlignment.CENTER);
            border(guideLabel, colors);

            guideValue = wb.createCellStyle();
            guideValue.setFont(bodyFont);
            guideValue.setWrapText(true);
            guideValue.setVerticalAlignment(VerticalAlignment.CENTER);
            border(guideValue, colors);
        }

        private void border(XSSFCellStyle style, DefaultIndexedColorMap colors) {
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            XSSFColor border = new XSSFColor(GREY_BORDER, colors);
            style.setBottomBorderColor(border);
            style.setTopBorderColor(border);
            style.setLeftBorderColor(border);
            style.setRightBorderColor(border);
        }
    }
}
