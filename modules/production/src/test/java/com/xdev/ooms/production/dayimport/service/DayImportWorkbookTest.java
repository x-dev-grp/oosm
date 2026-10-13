package com.xdev.ooms.production.dayimport.service;

import com.xdev.ooms.sharedkernel.ports.CompanyProfileSnapshot;
import org.junit.jupiter.api.Test;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class DayImportWorkbookTest {
    @Test void headingsAreReadableAndImportableInEverySupportedLanguage() throws Exception {
        var factory = new DayImportTemplateFactory();
        var reader = new DayImportWorkbookReader();
        var date = LocalDate.of(2026, 9, 5);
        for (String language : Set.of("fr", "en", "ar")) {
            byte[] bytes = factory.sampleTemplate(date, language);
            try (var xlsx = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
                var suppliers = xlsx.getSheet(DayImportColumnLabels.sheetName("Suppliers", language)).getRow(0);
                assertNotNull(suppliers, language);
                assertNull(xlsx.getSheet("OilSaleContainers"), language);
                assertEquals(DayImportColumnLabels.heading("Suppliers", "supplierKey", language),
                        suppliers.getCell(0).getStringCellValue());
                assertEquals(DayImportColumnLabels.heading("Suppliers", "name", language),
                        suppliers.getCell(1).getStringCellValue());
                assertNotEquals("supplierKey", suppliers.getCell(0).getStringCellValue());
                assertEquals(switch (language) {
                    case "en" -> "Supplier code";
                    case "ar" -> "رمز المورّد";
                    default -> "Code fournisseur";
                }, suppliers.getCell(0).getStringCellValue());
                for (int i = 0; i < xlsx.getNumberOfSheets(); i++) {
                    var sheet = xlsx.getSheetAt(i);
                    if (i == 0) continue;
                    var header = sheet.getRow(0);
                    var unique = new HashSet<String>();
                    for (var cell : header) {
                        String displayed = cell.getStringCellValue();
                        String canonical = DayImportColumnLabels.canonicalSheet(sheet.getSheetName());
                        String key = DayImportColumnLabels.key(canonical, displayed);
                        assertFalse(canonical.equals("Payments") && key.equals("paymentdate"), displayed);
                        assertFalse(canonical.equals("QcResults") && key.equals("oilqc"), displayed);
                        assertFalse(displayed.matches(".*[a-z][A-Z].*"), sheet.getSheetName() + ": " + displayed);
                        assertTrue(unique.add(displayed), sheet.getSheetName() + ": " + displayed);
                    }
                }
                var meta = xlsx.getSheet(DayImportColumnLabels.sheetName("ImportMeta", language));
                assertEquals(DayImportColumnLabels.heading("ImportMetaValues", "businessDate", language),
                        meta.getRow(1).getCell(0).getStringCellValue());
                assertEquals(DayImportColumnLabels.heading("ImportMetaValues", "fileCode", language),
                        meta.getRow(2).getCell(0).getStringCellValue());
                assertTrue(meta.getRow(2).getCell(1).getStringCellValue().matches("[A-Z2-9]{4}"));
                for (int r = 3; r <= meta.getLastRowNum(); r++) {
                    assertEquals("", meta.getRow(r).getCell(0).getStringCellValue(), language + " day row " + r);
                }
            }
            var parsed = reader.read(new ByteArrayInputStream(bytes));
            assertEquals(date, parsed.getBusinessDate());
            assertEquals(2, parsed.getTemplateVersion());
            assertEquals("SUP-001", parsed.getSuppliers().getFirst().supplierKey);
            assertTrue(parsed.getReceptions().getFirst().externalRef.matches("R-20260905-[A-Z2-9]{4}-001"),
                    parsed.getReceptions().getFirst().externalRef);
            String oilReception = parsed.getReceptions().get(1).externalRef;
            assertTrue(oilReception.endsWith("-002"), oilReception);
            assertEquals(oilReception, parsed.getPayments().getFirst().receptionExternalRef);
            assertTrue(parsed.getPayments().getFirst().externalRef.matches("P-20260905-[A-Z2-9]{4}-001"));
            assertTrue(parsed.getOilSales().getFirst().externalRef.matches("S-20260905-[A-Z2-9]{4}-001"));
        }
    }

    @Test void blankTemplateOffersTenantRecordsWithoutImportingThem() throws Exception {
        var reference = new DayImportReferenceData(
                List.of("Cuve A", "Cuve B"), List.of("Ali Ben", " ali ben ", "20000000"),
                List.of("Sfax"), List.of("P1"), List.of("Apporteur"), List.of("Chemlali"),
                List.of("Bidon 5L"), List.of("Acidite"), List.of(4, 9));
        byte[] bytes = new DayImportTemplateFactory().blankTemplate("fr", reference);
        try (var xlsx = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var tanks = xlsx.getSheet("Cuves");
            assertEquals("Cuve A", tanks.getRow(1).getCell(0).getStringCellValue());
            assertEquals("Cuve B", tanks.getRow(2).getCell(0).getStringCellValue());
            var lists = xlsx.getSheet("Données existantes");
            assertEquals("Fournisseurs", lists.getRow(0).getCell(0).getStringCellValue());
            assertEquals("20000000", lists.getRow(1).getCell(0).getStringCellValue());
            assertEquals("Ali Ben", lists.getRow(2).getCell(0).getStringCellValue());
            assertTrue(lists.getRow(3).getCell(0).getCellFormula().contains("Fournisseurs!$A$2"));
            assertEquals("Chemlali", lists.getRow(1).getCell(4).getStringCellValue());
            assertEquals("'Données existantes'!$E$2:$E$2", xlsx.getName("VarietyNames").getRefersToFormula());
            assertTrue(xlsx.getName("SupplierKeys").getRefersToFormula().startsWith("'Données existantes'!$A$2"));
            assertTrue(xlsx.getName("SaleRefs").getRefersToFormula().startsWith("'Ventes d''huile'!$A$2"));
        }
        var parsed = new DayImportWorkbookReader().read(new ByteArrayInputStream(bytes));
        assertTrue(parsed.getSuppliers().isEmpty());
        assertTrue(parsed.getRegions().isEmpty());
        assertTrue(parsed.getContainers().isEmpty());
        assertTrue(parsed.getQcRules().isEmpty());
    }

    @Test void guideShowsCompanyAndCriticalCellsAreProtected() throws Exception {
        var company = new CompanyProfileSnapshot("Huilerie Test", "Route de Tunis, Sfax", "1234567/A", "74000000",
                "", null, null, "", "", "", "", "", "", "");
        byte[] bytes = new DayImportTemplateFactory().blankTemplate("en", DayImportReferenceData.EMPTY, company);
        try (var xlsx = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var guideText = new StringBuilder();
            for (var row : xlsx.getSheetAt(0)) for (var cell : row) guideText.append(cell.getStringCellValue()).append('|');
            assertTrue(guideText.toString().contains("Huilerie Test"), guideText.toString());
            assertTrue(guideText.toString().contains("1234567/A"));
            assertFalse(guideText.toString().contains("Website"), "blank company fields are skipped");
            assertTrue(guideText.toString().contains("Unique code"));
            assertTrue(guideText.toString().contains("Red cells"));
            assertTrue(guideText.toString().contains("IMPORTANT — Mandatory rules"));
            assertTrue(guideText.toString().contains("9. Always check the validation report"));
            assertTrue(guideText.toString().contains("your company's responsibility"));
            assertTrue(guideText.indexOf("Mandatory rules") < guideText.indexOf("Huilerie Test"),
                    "rules come before everything else");
            assertTrue(xlsx.isStructureLocked());

            var receptions = xlsx.getSheet("Receptions");
            assertTrue(receptions.getProtect());
            assertTrue(receptions.isSortLocked(), "sorting would detach rows from their generated reference");
            var header = receptions.getRow(0);
            var generated = header.getCell(0).getCellStyle().getFillForegroundColorColor();
            var required = header.getCell(1).getCellStyle().getFillForegroundColorColor();
            var optional = header.getCell(12).getCellStyle().getFillForegroundColorColor();
            var code = xlsx.getSheet("Suppliers").getRow(0).getCell(0).getCellStyle().getFillForegroundColorColor();
            assertNotEquals(generated.getARGBHex(), required.getARGBHex());
            assertNotEquals(code.getARGBHex(), required.getARGBHex());
            assertNotEquals(required.getARGBHex(), optional.getARGBHex());
            assertTrue(header.getCell(0).getCellStyle().getLocked());
            assertTrue(receptions.getRow(1).getCell(0).getCellStyle().getLocked(), "reference is generated");
            assertTrue(receptions.getRow(1).getCell(13).getCellStyle().getLocked(), "planned lot is generated");
            assertFalse(receptions.getRow(1).getCell(1).getCellStyle().getLocked());
            assertFalse(receptions.getColumnStyle(1).getLocked(), "rows past the prefilled block stay editable");
            assertTrue(receptions.getSheetConditionalFormatting().getNumConditionalFormattings() > 0);

            var tanks = xlsx.getSheet("Tanks");
            assertTrue(tanks.getRow(1).getCell(0).getCellStyle().getLocked());
            var day = xlsx.getSheet("Day");
            assertTrue(day.getRow(1).getCell(0).getCellStyle().getLocked());
            assertFalse(day.getRow(1).getCell(1).getCellStyle().getLocked());
        }
    }

    @Test void dropdownCodesAreValuesTheImporterAccepts() throws Exception {
        for (String method : DayImportTemplateFactory.PAYMENT_METHODS.split(",")) {
            assertDoesNotThrow(() -> com.xdev.ooms.sharedkernel.Enum.PaymentMethod.valueOf(method), method);
        }
        byte[] bytes = new DayImportTemplateFactory().blankTemplate("fr");
        try (var xlsx = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var range = xlsx.getName("ExpenseCategories").getRefersToFormula();
            assertTrue(range.startsWith("'Données existantes'!$H$2"), range);
            var lists = xlsx.getSheet("Données existantes");
            assertEquals("FUEL", lists.getRow(1).getCell(7).getStringCellValue());
        }
    }

    @Test void originalAttributeHeadingsRemainImportable() throws Exception {
        try (var xlsx = new XSSFWorkbook()) {
            var suppliers = xlsx.createSheet("Suppliers");
            var header = suppliers.createRow(0);
            header.createCell(0).setCellValue("supplierKey");
            header.createCell(1).setCellValue("name");
            header.createCell(2).setCellValue("lastname");
            var row = suppliers.createRow(1);
            row.createCell(0).setCellValue("SUP-OLD");
            row.createCell(1).setCellValue("Ali");
            row.createCell(2).setCellValue("Ben");
            var bytes = new ByteArrayOutputStream();
            xlsx.write(bytes);
            var parsed = new DayImportWorkbookReader().read(new ByteArrayInputStream(bytes.toByteArray()));
            assertEquals("SUP-OLD", parsed.getSuppliers().getFirst().supplierKey);
            assertEquals("Ali", parsed.getSuppliers().getFirst().name);
        }
    }

    @Test void olderWorkbooksWithVisibleVersionAndDatesStillImport() throws Exception {
        try (var xlsx = new XSSFWorkbook()) {
            var meta = xlsx.createSheet("ImportMeta");
            meta.createRow(0).createCell(0).setCellValue("key");
            String[][] values = {{"businessDate", "2026-09-05"}, {"timezone", "Europe/Paris"}, {"templateVersion", "2"}};
            for (int i = 0; i < values.length; i++) {
                var row = meta.createRow(i + 1);
                row.createCell(0).setCellValue(values[i][0]);
                row.createCell(1).setCellValue(values[i][1]);
            }
            var payments = xlsx.createSheet("Payments");
            var header = payments.createRow(0);
            String[] headings = {"receptionExternalRef", "amount", "paymentMethod", "externalRef", "paymentDate"};
            for (int i = 0; i < headings.length; i++) header.createCell(i).setCellValue(headings[i]);
            var row = payments.createRow(1);
            String[] cells = {"R-1", "10", "CASH", "P-1", "2026-09-04"};
            for (int i = 0; i < cells.length; i++) row.createCell(i).setCellValue(cells[i]);
            var bytes = new ByteArrayOutputStream();
            xlsx.write(bytes);
            var parsed = new DayImportWorkbookReader().read(new ByteArrayInputStream(bytes.toByteArray()));
            assertEquals(2, parsed.getTemplateVersion());
            assertEquals("Europe/Paris", parsed.getTimezone());
            assertEquals(LocalDate.of(2026, 9, 4), parsed.getPayments().getFirst().paymentDate);
        }
    }

    @Test void generatedSampleHasVersionedPaymentReferences() throws Exception {
        var factory = new DayImportTemplateFactory();
        var workbook = new DayImportWorkbookReader().read(new ByteArrayInputStream(factory.sampleTemplate(LocalDate.of(2026,9,5))));
        assertEquals(2, workbook.getTemplateVersion());
        assertFalse(workbook.getPayments().isEmpty());
        assertTrue(workbook.getPayments().getFirst().externalRef.startsWith("P-20260905-"));
        assertEquals(workbook.getBusinessDate(), workbook.getPayments().getFirst().paymentDate);
    }

    @Test void referencesAndPlannedLotsAreComputedInTheWorkbook() throws Exception {
        var reference = new DayImportReferenceData(List.of("Cuve A"), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(4, 9));
        byte[] template = new DayImportTemplateFactory().blankTemplate("fr", reference);
        byte[] filled;
        String fileCode;
        try (var xlsx = new XSSFWorkbook(new ByteArrayInputStream(template))) {
            var meta = xlsx.getSheet("Journée");
            meta.getRow(1).getCell(1).setCellValue("2026-09-26");
            fileCode = meta.getRow(2).getCell(1).getStringCellValue();
            var receptions = xlsx.getSheet("Réceptions");
            String[][] rows = {{"OLIVE", "OC", "SIMPLE_RECEPTION"}, {"OIL", "OB", "OIL_PURCHASE"}};
            for (int i = 0; i < rows.length; i++) {
                var row = receptions.getRow(i + 1);
                for (int c = 0; c < 3; c++) row.getCell(c + 1).setCellValue(rows[i][c]);
                row.getCell(5).setCellValue("SUP-1");
            }
            var evaluator = xlsx.getCreationHelper().createFormulaEvaluator();
            assertEquals("R-20260926-" + fileCode + "-002",
                    evaluator.evaluate(receptions.getRow(2).getCell(0)).getStringValue());
            assertEquals("0004OC26", evaluator.evaluate(receptions.getRow(1).getCell(13)).getStringValue());
            assertEquals("0009OB26", evaluator.evaluate(receptions.getRow(2).getCell(13)).getStringValue());
            assertEquals("", evaluator.evaluate(receptions.getRow(3).getCell(0)).getStringValue(), "untouched rows stay blank");

            var validations = receptions.getDataValidations();
            assertTrue(validations.stream().anyMatch(v -> String.valueOf(v.getValidationConstraint().getFormula1()).contains("INDIRECT")));
            assertTrue(validations.stream().anyMatch(v -> v.getRegions().getCellRangeAddress(0).getFirstColumn() == 8
                    && v.getValidationConstraint().getValidationType() == org.apache.poi.ss.usermodel.DataValidationConstraint.ValidationType.DECIMAL));

            var out = new ByteArrayOutputStream();
            xlsx.write(out);
            filled = out.toByteArray();
        }
        var parsed = new DayImportWorkbookReader().read(new ByteArrayInputStream(filled));
        assertEquals(2, parsed.getReceptions().size());
        assertEquals("R-20260926-" + fileCode + "-001", parsed.getReceptions().getFirst().externalRef);
    }
    @Test void malformedNumberIsNotSilentlyTreatedAsBlank() throws Exception {
        try (var workbook = new XSSFWorkbook()) {
            var sheet=workbook.createSheet("Payments");var header=sheet.createRow(0);header.createCell(0).setCellValue("amount");
            sheet.createRow(1).createCell(0).setCellValue("not a number");
            var bytes=new ByteArrayOutputStream();workbook.write(bytes);
            assertThrows(IllegalArgumentException.class, () -> new DayImportWorkbookReader().read(new ByteArrayInputStream(bytes.toByteArray())));
        }
    }
}
