package com.xdev.ooms.production.dayimport.service;

import com.xdev.ooms.sharedkernel.Enum.ExpenseCategory;
import com.xdev.ooms.sharedkernel.Enum.PaymentMethod;
import com.xdev.ooms.sharedkernel.ports.CompanyProfileSnapshot;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.usermodel.Workbook;
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
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFName;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Builds blank / sample day-import workbooks.
 * Sheet names + headers stay aligned with {@link DayImportWorkbookReader}.
 * Styling matches OOSM (primary #4680FF + olive production accent).
 */
@Component
public class DayImportTemplateFactory {

    static final int LIST_ROWS = 200;
    /** Must stay valid {@link PaymentMethod} names; OIL and MIXED need details the workbook cannot carry. */
    static final String PAYMENT_METHODS = String.join(",",
            PaymentMethod.CASH.name(), PaymentMethod.CHEQUE.name(), PaymentMethod.TRANSFER.name());
    static final String TEMPLATE_VERSION_PROPERTY = "OOSMTemplateVersion";
    static final int TEMPLATE_VERSION = 2;

    public byte[] blankTemplate() throws IOException {
        return blankTemplate("fr");
    }

    public byte[] blankTemplate(String language) throws IOException {
        return blankTemplate(language, DayImportReferenceData.EMPTY);
    }

    public byte[] blankTemplate(String language, DayImportReferenceData reference) throws IOException {
        return blankTemplate(language, reference, null);
    }

    public byte[] blankTemplate(String language, DayImportReferenceData reference, CompanyProfileSnapshot company)
            throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            buildWorkbook(wb, null, false, language, reference, company);
            wb.write(out);
            return out.toByteArray();
        }
    }

    public byte[] sampleTemplate(LocalDate businessDate) throws IOException {
        return sampleTemplate(businessDate, "fr");
    }

    public byte[] sampleTemplate(LocalDate businessDate, String language) throws IOException {
        return sampleTemplate(businessDate, language, null);
    }

    public byte[] sampleTemplate(LocalDate businessDate, String language, CompanyProfileSnapshot company)
            throws IOException {
        LocalDate day = businessDate != null ? businessDate : LocalDate.now();
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            buildWorkbook(wb, day, true, language, DayImportReferenceData.EMPTY, company);
            wb.write(out);
            return out.toByteArray();
        }
    }

    private void buildWorkbook(XSSFWorkbook wb, LocalDate businessDate, boolean sample, String language,
                               DayImportReferenceData reference, CompanyProfileSnapshot company) {
        Styles styles = new Styles(wb, DayImportColumnLabels.language(language));
        wb.getProperties().getCustomProperties().addProperty(TEMPLATE_VERSION_PROPERTY, TEMPLATE_VERSION);

        writeGuide(wb, styles, businessDate, sample, company);
        String fileCode = newFileCode();
        Sheet meta = writeImportMeta(wb, styles, businessDate, fileCode);
        String day = ref(meta) + "!$B$2";
        String code = ref(meta) + "!$B$3";

        Sheet regions = createSheet(wb, styles, "Regions");
        writeHeaders(regions, styles, "name", "description");

        Sheet parcels = createSheet(wb, styles, "Parcels");
        writeHeaders(parcels, styles, "name", "description");

        Sheet supplierTypes = createSheet(wb, styles, "SupplierTypes");
        writeHeaders(supplierTypes, styles, "name", "description");

        Sheet qcRules = createSheet(wb, styles, "QcRules");
        writeHeaders(qcRules, styles,
                "ruleKey", "ruleName", "oilQc", "ruleType", "minValue", "maxValue", "ruleTextValue", "description");

        Sheet suppliers = createSheet(wb, styles, "Suppliers");
        writeHeaders(suppliers, styles,
                "supplierKey", "name", "lastname", "phone", "matriculeFiscal", "regionName", "supplierTypeName");

        Sheet containers = createSheet(wb, styles, "OilContainers");
        writeHeaders(containers, styles,
                "containerKey", "name", "capacityInLiters", "stockQuantity", "buyPrice", "sellingPrice");

        // Dropdown source only — tanks must already exist in the app.
        Sheet storageUnits = createSheet(wb, styles, "StorageUnits");
        writeHeaders(storageUnits, styles, "storageUnitKey", "name");

        Sheet receptions = createSheet(wb, styles, "Receptions");
        writeHeaders(receptions, styles,
                "externalRef", "deliveryType", "oliveOilType", "varietyName", "operationType",
                "supplierKey", "regionName", "parcelName", "poidsNet", "oilQuantity", "unitPrice",
                "storageUnitKey", "description", "plannedLotNumber");

        // Oil/olive check type is derived from the reception.
        Sheet qcResults = createSheet(wb, styles, "QcResults");
        writeHeaders(qcResults, styles, "receptionExternalRef", "ruleKey", "value");

        // Payment date is the business date.
        Sheet payments = createSheet(wb, styles, "Payments");
        writeHeaders(payments, styles, "receptionExternalRef", "amount", "paymentMethod", "externalRef");

        Sheet oilSales = createSheet(wb, styles, "OilSales");
        writeHeaders(oilSales, styles,
                "externalRef", "invoiceNumber", "supplierKey", "storageUnitKey", "quantity", "unitPrice",
                "currency", "paymentMethod", "qualityGrade", "paidAmount", "description");

        Sheet oilSaleContainers = createSheet(wb, styles, "OilSaleContainers");
        writeHeaders(oilSaleContainers, styles, "saleExternalRef", "containerKey", "count");

        Sheet expenses = createSheet(wb, styles, "Expenses");
        writeHeaders(expenses, styles,
                "externalRef", "amount", "object", "purchaseNature", "category", "paymentMethod",
                "vendor", "invoiceRef", "notes");

        if (sample) {
            fillSample(regions, parcels, supplierTypes, qcRules, suppliers, containers, storageUnits,
                    receptions, qcResults, payments, oilSales, oilSaleContainers, expenses, styles,
                    businessDate, fileCode);
        } else {
            for (int i = 0; i < reference.tanks().size(); i++) {
                String tank = reference.tanks().get(i);
                sample(storageUnits, styles, i + 1, tank, tank);
            }
        }
        int tankRows = Math.max(LIST_ROWS, reference.tanks().size());

        // The reader ignores this sheet: it only feeds dropdowns with existing records plus rows added in this file.
        Sheet lists = createSheet(wb, styles, "Lists");
        writeListHeaders(lists, styles);
        writeList(wb, lists, styles, 0, "SupplierKeys", reference.suppliers(), suppliers);
        writeList(wb, lists, styles, 1, "RegionNames", reference.regions(), regions);
        writeList(wb, lists, styles, 2, "ParcelNames", reference.parcels(), parcels);
        writeList(wb, lists, styles, 3, "SupplierTypeNames", reference.supplierTypes(), supplierTypes);
        writeList(wb, lists, styles, 4, "VarietyNames", reference.varieties(), null);
        writeList(wb, lists, styles, 5, "ContainerKeys", reference.containers(), containers);
        writeList(wb, lists, styles, 6, "QcRuleKeys", reference.qcRules(), qcRules);
        writeList(wb, lists, styles, 7, "ExpenseCategories",
                Arrays.stream(ExpenseCategory.values()).map(Enum::name).toList(), null);
        writeLotSequences(wb, lists, styles, reference.nextLotSequences());
        writeList(wb, lists, styles, 9, "Ops_OLIVE", OLIVE_OPERATIONS, null);
        writeList(wb, lists, styles, 10, "Ops_OIL", OIL_OPERATIONS, null);
        for (int c = 8; c <= 10; c++) {
            lists.setColumnHidden(c, true);
        }

        autoReferences(receptions, styles, 0, 1, 12, "R", day, code);
        plannedLots(receptions, styles, 13, day);
        autoReferences(payments, styles, 3, 0, 2, "P", day, code);
        autoReferences(oilSales, styles, 0, 1, 10, "S", day, code);
        autoReferences(expenses, styles, 0, 1, 8, "E", day, code);
        wb.setForceFormulaRecalculation(true);

        createNamedRange(wb, "StorageUnitKeys", ref(storageUnits) + "!$A$2:$A$" + (tankRows + 1));
        createNamedRange(wb, "ReceptionRefs", ref(receptions) + "!$A$2:$A$" + (LIST_ROWS + 1));
        createNamedRange(wb, "SaleRefs", ref(oilSales) + "!$A$2:$A$" + (LIST_ROWS + 1));

        addListValidation(suppliers, 1, LIST_ROWS, 5, "RegionNames");
        addListValidation(suppliers, 1, LIST_ROWS, 6, "SupplierTypeNames");

        addExplicitList(receptions, 1, LIST_ROWS, 1, "OLIVE,OIL");
        addExplicitList(receptions, 1, LIST_ROWS, 2, "OC,OB");
        addListValidation(receptions, 1, LIST_ROWS, 3, "VarietyNames");
        addStrictList(receptions, 4, "INDIRECT(\"Ops_\"&$B2)", styles, "validation.operation");
        addListValidation(receptions, 1, LIST_ROWS, 5, "SupplierKeys");
        addListValidation(receptions, 1, LIST_ROWS, 6, "RegionNames");
        addListValidation(receptions, 1, LIST_ROWS, 7, "ParcelNames");
        addListValidation(receptions, 1, LIST_ROWS, 11, "StorageUnitKeys");
        addNumberValidation(receptions, 8, NumberRule.POSITIVE, styles);
        addNumberValidation(receptions, 9, NumberRule.POSITIVE, styles);
        addNumberValidation(receptions, 10, NumberRule.POSITIVE, styles);
        highlightMissing(receptions, "AND($B2=\"OLIVE\",LEN(I2)=0)", "I2:I" + (LIST_ROWS + 1));
        highlightMissing(receptions, "AND($B2=\"OIL\",LEN(J2)=0)", "J2:L" + (LIST_ROWS + 1));

        addNumberValidation(payments, 1, NumberRule.POSITIVE, styles);
        addNumberValidation(oilSales, 4, NumberRule.POSITIVE, styles);
        addNumberValidation(oilSales, 5, NumberRule.POSITIVE, styles);
        addNumberValidation(oilSales, 9, NumberRule.NON_NEGATIVE, styles);
        addNumberValidation(oilSaleContainers, 2, NumberRule.WHOLE, styles);
        addNumberValidation(expenses, 1, NumberRule.POSITIVE, styles);
        addNumberValidation(containers, 2, NumberRule.POSITIVE, styles);
        addNumberValidation(containers, 3, NumberRule.NON_NEGATIVE, styles);
        addNumberValidation(containers, 4, NumberRule.NON_NEGATIVE, styles);
        addNumberValidation(containers, 5, NumberRule.NON_NEGATIVE, styles);
        addDateValidation(meta, styles);

        addListValidation(qcResults, 1, LIST_ROWS, 0, "ReceptionRefs");
        addListValidation(qcResults, 1, LIST_ROWS, 1, "QcRuleKeys");

        addListValidation(payments, 1, LIST_ROWS, 0, "ReceptionRefs");
        addExplicitList(payments, 1, LIST_ROWS, 2, PAYMENT_METHODS);

        addListValidation(oilSales, 1, LIST_ROWS, 2, "SupplierKeys");
        addListValidation(oilSales, 1, LIST_ROWS, 3, "StorageUnitKeys");
        addExplicitList(oilSales, 1, LIST_ROWS, 6, "TND,EUR,USD");
        addExplicitList(oilSales, 1, LIST_ROWS, 7, PAYMENT_METHODS);
        addExplicitList(oilSales, 1, LIST_ROWS, 8, "EXTRA_VIRGIN,VIRGIN,LAMPANTE,REFINED,POMACE");

        addListValidation(oilSaleContainers, 1, LIST_ROWS, 0, "SaleRefs");
        addListValidation(oilSaleContainers, 1, LIST_ROWS, 1, "ContainerKeys");

        addListValidation(expenses, 1, LIST_ROWS, 4, "ExpenseCategories");
        addExplicitList(expenses, 1, LIST_ROWS, 5, PAYMENT_METHODS);
        addExplicitList(qcRules, 1, LIST_ROWS, 2, "true,false");
        addExplicitList(qcRules, 1, LIST_ROWS, 3, "NUMERIC,STRING,BOOLEAN");

        lockContent(storageUnits, styles);
        lockContent(lists, styles);
        for (Sheet sheet : wb) {
            protect((XSSFSheet) sheet);
        }
        wb.lockStructure();
        wb.setActiveSheet(0);
    }

    /** Only cells styled as data stay editable; renaming headings or tabs would break the import. */
    private void protect(XSSFSheet sheet) {
        sheet.enableLocking();
        sheet.lockFormatColumns(false);
        sheet.lockFormatRows(false);
        sheet.lockFormatCells(false);
        sheet.lockAutoFilter(false);
    }

    private static final List<String> OLIVE_OPERATIONS = List.of("SIMPLE_RECEPTION", "OLIVE_PURCHASE", "EXCHANGE", "BASE");
    private static final List<String> OIL_OPERATIONS = List.of("OIL_PURCHASE");
    private static final String FILE_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    /** Makes references from two templates filled for the same day distinct. */
    private static String newFileCode() {
        StringBuilder code = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            code.append(FILE_CODE_ALPHABET.charAt(RANDOM.nextInt(FILE_CODE_ALPHABET.length())));
        }
        return code.toString();
    }

    /** yyyyMMdd whether the business date cell holds a real date or the text yyyy-MM-dd (TEXT() date codes are locale-specific). */
    private static String dateKey(String day) {
        return "IF(ISNUMBER(" + day + "),YEAR(" + day + ")*10000+MONTH(" + day + ")*100+DAY(" + day + "),SUBSTITUTE(" + day + ",\"-\",\"\"))";
    }

    static String sampleReference(String prefix, LocalDate day, String fileCode, int row) {
        return prefix + "-" + day.toString().replace("-", "") + "-" + fileCode + "-" + String.format("%03d", row);
    }

    /** Reference = prefix-date-file code-row, shown only once the row has content; sorting is locked so rows keep their reference. */
    private void autoReferences(Sheet sheet, Styles styles, int col, int firstInput, int lastInput,
                                String prefix, String day, String code) {
        for (int r = 1; r <= LIST_ROWS; r++) {
            int excelRow = r + 1;
            String inputs = CellReference.convertNumToColString(firstInput) + excelRow + ":"
                    + CellReference.convertNumToColString(lastInput) + excelRow;
            Cell cell = listRow(sheet, r).createCell(col);
            cell.setCellFormula("IF(OR(COUNTA(" + inputs + ")=0," + day + "=\"\"),\"\",\"" + prefix + "-\"&"
                    + dateKey(day) + "&\"-\"&" + code + "&\"-\"&TEXT(ROW()-1,\"000\"))");
            cell.setCellStyle(styles.locked);
        }
        sheet.setDefaultColumnStyle(col, styles.locked);
    }

    /** Mirrors UnifiedDeliveryService lot numbers: next free sequence + OC/OB + two-digit year. */
    private void plannedLots(Sheet receptions, Styles styles, int col, String day) {
        for (int r = 1; r <= LIST_ROWS; r++) {
            int n = r + 1;
            Cell cell = listRow(receptions, r).createCell(col);
            cell.setCellFormula("IF(OR($B" + n + "=\"\",$C" + n + "=\"\"," + day + "=\"\"),\"\","
                    + "TEXT(INDEX(LotSequences,COUNTIF($B$2:$B" + n + ",\"?*\")),\"0000\")"
                    + "&SUBSTITUTE(SUBSTITUTE(UPPER($C" + n + "),\"HC\",\"OC\"),\"HB\",\"OB\")"
                    + "&RIGHT(IF(ISNUMBER(" + day + "),YEAR(" + day + "),LEFT(" + day + ",4)),2))");
            cell.setCellStyle(styles.locked);
        }
        receptions.setDefaultColumnStyle(col, styles.locked);
    }

    private void writeLotSequences(XSSFWorkbook wb, Sheet lists, Styles styles, List<Integer> sequences) {
        List<Integer> values = sequences.isEmpty()
                ? IntStream.rangeClosed(1, LIST_ROWS).boxed().toList()
                : sequences;
        for (int i = 0; i < values.size(); i++) {
            Cell cell = listRow(lists, i + 1).createCell(8);
            cell.setCellValue(values.get(i));
            cell.setCellStyle(styles.data);
        }
        createNamedRange(wb, "LotSequences", ref(lists) + "!$I$2:$I$" + (values.size() + 1));
    }

    private enum NumberRule { POSITIVE, NON_NEGATIVE, WHOLE }

    private void addNumberValidation(Sheet sheet, int col, NumberRule rule, Styles styles) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createNumericConstraint(
                rule == NumberRule.WHOLE ? DataValidationConstraint.ValidationType.INTEGER : DataValidationConstraint.ValidationType.DECIMAL,
                rule == NumberRule.NON_NEGATIVE ? DataValidationConstraint.OperatorType.GREATER_OR_EQUAL : DataValidationConstraint.OperatorType.GREATER_THAN,
                "0", null);
        String message = switch (rule) {
            case POSITIVE -> "validation.positive";
            case NON_NEGATIVE -> "validation.nonNegative";
            case WHOLE -> "validation.whole";
        };
        addStrict(sheet, col, constraint, styles, message);
    }

    private void addDateValidation(Sheet meta, Styles styles) {
        DataValidationHelper helper = meta.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createDateConstraint(
                DataValidationConstraint.OperatorType.BETWEEN, "DATE(2000,1,1)", "TODAY()", "yyyy-MM-dd");
        DataValidation validation = helper.createValidation(constraint, new CellRangeAddressList(1, 1, 1, 1));
        validation.setShowErrorBox(true);
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        validation.createErrorBox(DayImportColumnLabels.guide("validation.title", styles.language),
                DayImportColumnLabels.guide("validation.date", styles.language));
        meta.addValidationData(validation);
    }

    private void addStrictList(Sheet sheet, int col, String formula, Styles styles, String messageKey) {
        addStrict(sheet, col, sheet.getDataValidationHelper().createFormulaListConstraint(formula), styles, messageKey);
    }

    private void addStrict(Sheet sheet, int col, DataValidationConstraint constraint, Styles styles, String messageKey) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidation validation = helper.createValidation(constraint, new CellRangeAddressList(1, LIST_ROWS, col, col));
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        validation.createErrorBox(DayImportColumnLabels.guide("validation.title", styles.language),
                DayImportColumnLabels.guide(messageKey, styles.language));
        sheet.addValidationData(validation);
    }

    /** Fills a cell red while {@code formula} (written for row 2 of {@code range}) holds. */
    private void highlightMissing(Sheet sheet, String formula, String range) {
        SheetConditionalFormatting formatting = sheet.getSheetConditionalFormatting();
        ConditionalFormattingRule rule = formatting.createConditionalFormattingRule(formula);
        PatternFormatting fill = rule.createPatternFormatting();
        fill.setFillBackgroundColor(IndexedColors.ROSE.getIndex());
        fill.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
        formatting.addConditionalFormatting(new CellRangeAddress[]{CellRangeAddress.valueOf(range)}, rule);
    }

    /** A row counts as started once any typed column has content; generated columns show "" until then. */
    private void highlightRequired(Sheet sheet, String canonical, String[] headers) {
        Set<String> required = REQUIRED_COLUMNS.getOrDefault(canonical, Set.of());
        Set<String> codes = REFERENCE_COLUMNS.getOrDefault(canonical, Set.of());
        Set<String> generated = AUTO_COLUMNS.getOrDefault(canonical, Set.of());
        int lastInput = -1;
        for (int i = 0; i < headers.length; i++) {
            if (!generated.contains(headers[i])) lastInput = i;
        }
        if (lastInput < 0) return;
        String started = "SUMPRODUCT(--(LEN($A2:$" + CellReference.convertNumToColString(lastInput) + "2)>0))>0";
        for (int i = 0; i < headers.length; i++) {
            if (!required.contains(headers[i]) && !codes.contains(headers[i])) continue;
            String column = CellReference.convertNumToColString(i);
            highlightMissing(sheet, "AND(" + started + ",LEN(" + column + "2)=0)",
                    column + "2:" + column + (LIST_ROWS + 1));
        }
    }

    private void lockContent(Sheet sheet, Styles styles) {
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;
            for (Cell cell : row) {
                cell.setCellStyle(styles.locked);
            }
        }
        for (int c = 0; c < headerCount(sheet); c++) {
            sheet.setDefaultColumnStyle(c, styles.locked);
        }
    }

    private int headerCount(Sheet sheet) {
        Row header = sheet.getRow(0);
        return header == null ? 0 : header.getLastCellNum();
    }

    private void writeGuide(XSSFWorkbook wb, Styles styles, LocalDate businessDate, boolean sample,
                            CompanyProfileSnapshot company) {
        String lang = styles.language;
        Sheet guide = wb.createSheet(DayImportColumnLabels.sheetName("Guide", lang));
        guide.setColumnWidth(0, 24 * 256);
        guide.setColumnWidth(1, 96 * 256);
        guide.setColumnWidth(2, 24 * 256);
        if ("ar".equals(styles.language)) {
            guide.setRightToLeft(true);
        }

        Row title = guide.createRow(0);
        title.setHeightInPoints(28);
        Cell titleCell = title.createCell(0);
        titleCell.setCellValue("OOSM — " + DayImportColumnLabels.guide("title", styles.language));
        titleCell.setCellStyle(styles.title);
        guide.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

        Row subtitle = guide.createRow(1);
        subtitle.setHeightInPoints(20);
        Cell sub = subtitle.createCell(0);
        sub.setCellValue(DayImportColumnLabels.guide("subtitle", styles.language));
        sub.setCellStyle(styles.subtitle);
        guide.addMergedRegion(new CellRangeAddress(1, 1, 0, 1));

        int r = 3;
        r = writeRules(guide, styles, r);
        r++;
        if (company != null) {
            addLogo(wb, guide, company);
            r = guideSection(guide, styles, r, DayImportColumnLabels.guide("company", lang));
            r = guideOptionalPair(guide, styles, r, "company.name", company.legalName());
            r = guideOptionalPair(guide, styles, r, "company.address", company.address());
            r = guideOptionalPair(guide, styles, r, "company.taxId", company.taxId());
            r = guideOptionalPair(guide, styles, r, "company.phone", company.phone());
            r = guideOptionalPair(guide, styles, r, "company.website", company.website());
        }
        r = guidePair(guide, styles, r, DayImportColumnLabels.guide("generated", lang), LocalDate.now().toString());
        r++;

        r = guideSection(guide, styles, r, DayImportColumnLabels.guide("legend", lang));
        r = legendRow(guide, styles, r, styles.lockedHeader, "legend.locked");
        r = legendRow(guide, styles, r, styles.referenceHeader, "legend.reference");
        r = legendRow(guide, styles, r, styles.requiredHeader, "legend.required");
        r = legendRow(guide, styles, r, styles.header, "legend.optional");
        r++;

        r = guideSection(guide, styles, r, DayImportColumnLabels.guide("instructions", lang));
        for (String key : List.of("workflow", "day", "existing", "masters", "tanks", "references", "lot", "missing", "duplicates")) {
            r = guidePair(guide, styles, r, DayImportColumnLabels.guide(key + ".label", lang),
                    DayImportColumnLabels.guide(key, lang));
        }
        if (sample) {
            guidePair(guide, styles, r, DayImportColumnLabels.guide("sample.label", lang),
                    DayImportColumnLabels.guide("sample", lang)
                            + (businessDate != null ? businessDate : LocalDate.now()));
        }
    }

    private static final List<String> RULES = List.of(
            "template", "oneDay", "structure", "references", "dropdowns", "formats", "tanks", "lot", "review");

    private int writeRules(Sheet sheet, Styles styles, int rowIdx) {
        int r = guideBlock(sheet, rowIdx, styles.rulesTitle, DayImportColumnLabels.guide("rules", styles.language), 22);
        for (int i = 0; i < RULES.size(); i++) {
            String text = (i + 1) + ". " + DayImportColumnLabels.guide("rules." + RULES.get(i), styles.language);
            r = guideBlock(sheet, r, styles.rule, text, 30);
        }
        return guideBlock(sheet, r, styles.ruleEmphasis,
                DayImportColumnLabels.guide("rules.responsibility", styles.language), 44);
    }

    private int guideSection(Sheet sheet, Styles styles, int rowIdx, String title) {
        return guideBlock(sheet, rowIdx, styles.section, title, 22);
    }

    /** One text cell merged across the Guide's two columns; merged cells do not auto-fit, hence the fixed height. */
    private int guideBlock(Sheet sheet, int rowIdx, CellStyle style, String text, float height) {
        Row row = sheet.createRow(rowIdx);
        row.setHeightInPoints(height);
        for (int c = 0; c < 2; c++) {
            row.createCell(c).setCellStyle(style);
        }
        row.getCell(0).setCellValue(text);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 1));
        return rowIdx + 1;
    }

    private int guideOptionalPair(Sheet sheet, Styles styles, int rowIdx, String labelKey, String value) {
        if (value == null || value.isBlank()) return rowIdx;
        return guidePair(sheet, styles, rowIdx, DayImportColumnLabels.guide(labelKey, styles.language), value.trim());
    }

    private int legendRow(Sheet sheet, Styles styles, int rowIdx, CellStyle swatch, String key) {
        int next = guidePair(sheet, styles, rowIdx, DayImportColumnLabels.guide(key + ".label", styles.language),
                DayImportColumnLabels.guide(key, styles.language));
        sheet.getRow(rowIdx).getCell(0).setCellStyle(swatch);
        return next;
    }

    /** Best effort: an unreadable or unsupported logo just leaves the Guide without one. */
    private void addLogo(XSSFWorkbook wb, Sheet guide, CompanyProfileSnapshot company) {
        String data = company.logoBase64();
        String type = company.logoContentType() == null ? "" : company.logoContentType().toLowerCase(Locale.ROOT);
        if (data == null || data.isBlank()) return;
        int format = type.contains("png") ? Workbook.PICTURE_TYPE_PNG
                : type.contains("jpeg") || type.contains("jpg") ? Workbook.PICTURE_TYPE_JPEG : -1;
        if (format < 0) return;
        try {
            byte[] bytes = Base64.getMimeDecoder().decode(data.substring(data.indexOf(',') + 1));
            int picture = wb.addPicture(bytes, format);
            ClientAnchor anchor = wb.getCreationHelper().createClientAnchor();
            anchor.setCol1(2);
            anchor.setRow1(0);
            Picture logo = guide.createDrawingPatriarch().createPicture(anchor, picture);
            var size = logo.getImageDimension();
            double scale = Math.min(1.0, Math.min(160.0 / Math.max(1, size.width), 64.0 / Math.max(1, size.height)));
            logo.resize(scale);
        } catch (RuntimeException ignored) {
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

    private Sheet writeImportMeta(XSSFWorkbook wb, Styles styles, LocalDate businessDate, String fileCode) {
        Sheet meta = createSheet(wb, styles, "ImportMeta");
        writeHeaders(meta, styles, "key", "value");
        set(meta.getRow(1), 0, DayImportColumnLabels.heading("ImportMetaValues", "businessDate", styles.language), styles.locked);
        set(meta.getRow(1), 1, businessDate != null ? businessDate.toString() : "", styles.data);
        for (int r = 2; r <= meta.getLastRowNum(); r++) {
            meta.getRow(r).getCell(0).setCellStyle(styles.locked);
            meta.getRow(r).getCell(1).setCellStyle(styles.locked);
        }
        set(meta.getRow(2), 0, DayImportColumnLabels.heading("ImportMetaValues", "fileCode", styles.language), styles.locked);
        set(meta.getRow(2), 1, fileCode, styles.locked);
        meta.setDefaultColumnStyle(0, styles.locked);
        meta.setDefaultColumnStyle(1, styles.locked);
        return meta;
    }

    private void fillSample(Sheet regions, Sheet parcels, Sheet supplierTypes, Sheet qcRules, Sheet suppliers,
                            Sheet containers, Sheet storageUnits, Sheet receptions, Sheet qcResults,
                            Sheet payments, Sheet oilSales, Sheet oilSaleContainers, Sheet expenses,
                            Styles styles, LocalDate day, String fileCode) {
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

        String oilReception = sampleReference("R", day, fileCode, 2);
        String sampleSale = sampleReference("S", day, fileCode, 1);

        Row rOlive = receptions.getRow(1);
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
        set(qr, 0, oilReception, styles.data);
        set(qr, 1, "Acidite", styles.data);
        set(qr, 2, "0.4", styles.data);

        Row p = payments.getRow(1);
        set(p, 0, oilReception, styles.data);
        set(p, 1, "500", styles.data);
        set(p, 2, "CASH", styles.data);

        Row sale = oilSales.getRow(1);
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
        set(line, 0, sampleSale, styles.data);
        set(line, 1, "BIN-5L", styles.data);
        set(line, 2, "2", styles.data);

        Row e = expenses.getRow(1);
        set(e, 1, "50", styles.data);
        set(e, 2, "Fuel", styles.data);
        set(e, 3, "Diesel", styles.data);
        set(e, 4, "OTHER", styles.data);
        set(e, 5, "CASH", styles.data);
        set(e, 6, "Station", styles.data);
        set(e, 7, "EXP-INV-1", styles.data);
        set(e, 8, "", styles.data);
    }

    private Sheet createSheet(XSSFWorkbook wb, Styles styles, String canonical) {
        Sheet sheet = wb.createSheet(DayImportColumnLabels.sheetName(canonical, styles.language));
        sheet.createFreezePane(0, 1);
        sheet.setDefaultRowHeightInPoints(18);
        if ("ar".equals(styles.language)) {
            sheet.setRightToLeft(true);
        }
        return sheet;
    }

    /** Sheet name quoted for formulas (names may contain spaces, apostrophes or Arabic). */
    private static String ref(Sheet sheet) {
        return "'" + sheet.getSheetName().replace("'", "''") + "'";
    }

    private void writeHeaders(Sheet sheet, Styles styles, String... headers) {
        Row header = sheet.createRow(0);
        header.setHeightInPoints(22);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            String canonical = DayImportColumnLabels.canonicalSheet(sheet.getSheetName());
            String heading = DayImportColumnLabels.heading(canonical, headers[i], styles.language);
            cell.setCellValue(heading);
            cell.setCellStyle(headerStyle(styles, canonical, headers[i]));
            sheet.setColumnWidth(i, Math.min(38, Math.max(16, heading.length() + 4)) * 256);
            sheet.setDefaultColumnStyle(i, styles.data);
        }
        for (int r = 1; r <= 40; r++) {
            Row data = sheet.createRow(r);
            for (int c = 0; c < headers.length; c++) {
                Cell cell = data.createCell(c);
                cell.setCellStyle(r % 2 == 0 ? styles.alt : styles.data);
            }
        }
        String canonical = DayImportColumnLabels.canonicalSheet(sheet.getSheetName());
        if (!LOCKED_SHEETS.contains(canonical) && !"ImportMeta".equals(canonical)) {
            highlightRequired(sheet, canonical, headers);
        }
    }

    private static final Map<String, Set<String>> AUTO_COLUMNS = Map.of(
            "Receptions", Set.of("externalRef", "plannedLotNumber"),
            "Payments", Set.of("externalRef"),
            "OilSales", Set.of("externalRef"),
            "Expenses", Set.of("externalRef"));

    private static final Map<String, Set<String>> REFERENCE_COLUMNS = Map.of(
            "Suppliers", Set.of("supplierKey"),
            "OilContainers", Set.of("containerKey"),
            "QcRules", Set.of("ruleKey"));

    private static final Map<String, Set<String>> REQUIRED_COLUMNS = Map.ofEntries(
            Map.entry("ImportMeta", Set.of("value")),
            Map.entry("Regions", Set.of("name")),
            Map.entry("Parcels", Set.of("name")),
            Map.entry("SupplierTypes", Set.of("name")),
            Map.entry("QcRules", Set.of("ruleType")),
            Map.entry("Suppliers", Set.of("name")),
            Map.entry("OilContainers", Set.of("name")),
            Map.entry("Receptions", Set.of("deliveryType", "oliveOilType", "operationType", "supplierKey")),
            Map.entry("QcResults", Set.of("receptionExternalRef", "ruleKey", "value")),
            Map.entry("Payments", Set.of("receptionExternalRef", "amount", "paymentMethod")),
            Map.entry("OilSales", Set.of("storageUnitKey", "quantity", "unitPrice")),
            Map.entry("OilSaleContainers", Set.of("saleExternalRef", "containerKey", "count")),
            Map.entry("Expenses", Set.of("amount")));

    private static final Set<String> LOCKED_SHEETS = Set.of("StorageUnits", "Lists");

    private static CellStyle headerStyle(Styles styles, String sheet, String key) {
        if (LOCKED_SHEETS.contains(sheet) || ("ImportMeta".equals(sheet) && "key".equals(key))
                || AUTO_COLUMNS.getOrDefault(sheet, Set.of()).contains(key)) return styles.lockedHeader;
        if (REFERENCE_COLUMNS.getOrDefault(sheet, Set.of()).contains(key)) return styles.referenceHeader;
        if (REQUIRED_COLUMNS.getOrDefault(sheet, Set.of()).contains(key)) return styles.requiredHeader;
        return styles.header;
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

    private static final String[] LIST_COLUMNS =
            {"suppliers", "regions", "parcels", "supplierTypes", "varieties", "containers", "qcRules", "expenseCategories",
                    "nextLotNumbers", "oliveOperations", "oilOperations"};

    private void writeListHeaders(Sheet sheet, Styles styles) {
        Row header = sheet.createRow(0);
        header.setHeightInPoints(22);
        for (int i = 0; i < LIST_COLUMNS.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(DayImportColumnLabels.heading("Lists", LIST_COLUMNS[i], styles.language));
            cell.setCellStyle(styles.lockedHeader);
            sheet.setColumnWidth(i, 30 * 256);
        }
    }

    /**
     * Existing records first, then formulas mirroring column A of {@code sourceSheet} so values
     * typed into this file are selectable too.
     */
    private void writeList(XSSFWorkbook wb, Sheet sheet, Styles styles, int col, String rangeName,
                           List<String> existing, Sheet sourceSheet) {
        int rowIdx = 1;
        for (String value : existing) {
            set(listRow(sheet, rowIdx++), col, value, styles.data);
        }
        if (sourceSheet != null) {
            String source = ref(sourceSheet);
            for (int sourceRow = 2; sourceRow <= LIST_ROWS + 1; sourceRow++) {
                Cell cell = listRow(sheet, rowIdx++).createCell(col);
                cell.setCellFormula("IF(" + source + "!$A$" + sourceRow + "=\"\",\"\"," + source + "!$A$" + sourceRow + ")");
                cell.setCellStyle(styles.data);
            }
        }
        int lastRow = Math.max(rowIdx - 1, 1);
        String column = String.valueOf((char) ('A' + col));
        createNamedRange(wb, rangeName, ref(sheet) + "!$" + column + "$2:$" + column + "$" + (lastRow + 1));
    }

    private Row listRow(Sheet sheet, int rowIdx) {
        Row row = sheet.getRow(rowIdx);
        return row != null ? row : sheet.createRow(rowIdx);
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
        final String language;
        private static final byte[] PRIMARY = new byte[]{(byte) 0x46, (byte) 0x80, (byte) 0xFF};
        private static final byte[] OLIVE = new byte[]{(byte) 0x3D, (byte) 0x5A, (byte) 0x2C};
        private static final byte[] GREY_100 = new byte[]{(byte) 0xF8, (byte) 0xF9, (byte) 0xFA};
        private static final byte[] GREY_200 = new byte[]{(byte) 0xF3, (byte) 0xF5, (byte) 0xF7};
        private static final byte[] GREY_BORDER = new byte[]{(byte) 0xDB, (byte) 0xE0, (byte) 0xE5};
        private static final byte[] WHITE = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        private static final byte[] TEXT = new byte[]{(byte) 0x3E, (byte) 0x48, (byte) 0x53};
        private static final byte[] REFERENCE = new byte[]{(byte) 0xC6, (byte) 0x28, (byte) 0x28};
        private static final byte[] LOCKED_HEADER = new byte[]{(byte) 0x8A, (byte) 0x94, (byte) 0x9E};
        private static final byte[] LOCKED_FILL = new byte[]{(byte) 0xE9, (byte) 0xEC, (byte) 0xEF};
        private static final byte[] MUTED_TEXT = new byte[]{(byte) 0x6C, (byte) 0x75, (byte) 0x7D};
        private static final byte[] WARNING_FILL = new byte[]{(byte) 0xFF, (byte) 0xF4, (byte) 0xE5};
        private static final byte[] WARNING_TEXT = new byte[]{(byte) 0x7A, (byte) 0x3E, (byte) 0x00};

        final XSSFCellStyle title;
        final XSSFCellStyle subtitle;
        final XSSFCellStyle section;
        final XSSFCellStyle rulesTitle;
        final XSSFCellStyle rule;
        final XSSFCellStyle ruleEmphasis;
        final XSSFCellStyle header;
        final XSSFCellStyle requiredHeader;
        final XSSFCellStyle referenceHeader;
        final XSSFCellStyle lockedHeader;
        final XSSFCellStyle data;
        final XSSFCellStyle alt;
        final XSSFCellStyle locked;
        final XSSFCellStyle guideLabel;
        final XSSFCellStyle guideValue;

        Styles(XSSFWorkbook wb, String language) {
            this.language = language;
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

            requiredHeader = headerVariant(wb, header, OLIVE, colors);
            referenceHeader = headerVariant(wb, header, REFERENCE, colors);
            lockedHeader = headerVariant(wb, header, LOCKED_HEADER, colors);

            section = wb.createCellStyle();
            section.cloneStyleFrom(header);
            section.setFillForegroundColor(new XSSFColor(OLIVE, colors));
            section.setAlignment(HorizontalAlignment.LEFT);

            rulesTitle = wb.createCellStyle();
            rulesTitle.cloneStyleFrom(referenceHeader);
            rulesTitle.setAlignment(HorizontalAlignment.LEFT);

            XSSFFont warningFont = wb.createFont();
            warningFont.setFontHeightInPoints((short) 10);
            warningFont.setColor(new XSSFColor(WARNING_TEXT, colors));
            rule = wb.createCellStyle();
            rule.setFont(warningFont);
            rule.setFillForegroundColor(new XSSFColor(WARNING_FILL, colors));
            rule.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            rule.setWrapText(true);
            rule.setVerticalAlignment(VerticalAlignment.CENTER);
            border(rule, colors);

            XSSFFont warningBold = wb.createFont();
            warningBold.setBold(true);
            warningBold.setFontHeightInPoints((short) 10);
            warningBold.setColor(new XSSFColor(WARNING_TEXT, colors));
            ruleEmphasis = wb.createCellStyle();
            ruleEmphasis.cloneStyleFrom(rule);
            ruleEmphasis.setFont(warningBold);

            data = wb.createCellStyle();
            data.setFont(bodyFont);
            data.setVerticalAlignment(VerticalAlignment.CENTER);
            data.setLocked(false);
            border(data, colors);

            alt = wb.createCellStyle();
            alt.cloneStyleFrom(data);
            alt.setFillForegroundColor(new XSSFColor(GREY_200, colors));
            alt.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            alt.setLocked(false);

            XSSFFont lockedFont = wb.createFont();
            lockedFont.setItalic(true);
            lockedFont.setFontHeightInPoints((short) 10);
            lockedFont.setColor(new XSSFColor(MUTED_TEXT, colors));
            locked = wb.createCellStyle();
            locked.setFont(lockedFont);
            locked.setVerticalAlignment(VerticalAlignment.CENTER);
            locked.setFillForegroundColor(new XSSFColor(LOCKED_FILL, colors));
            locked.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            locked.setLocked(true);
            border(locked, colors);

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

        private static XSSFCellStyle headerVariant(XSSFWorkbook wb, XSSFCellStyle base, byte[] fill,
                                                   DefaultIndexedColorMap colors) {
            XSSFCellStyle style = wb.createCellStyle();
            style.cloneStyleFrom(base);
            style.setFillForegroundColor(new XSSFColor(fill, colors));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            return style;
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
