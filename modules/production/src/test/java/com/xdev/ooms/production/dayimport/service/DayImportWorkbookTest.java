package com.xdev.ooms.production.dayimport.service;

import org.junit.jupiter.api.Test;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.time.LocalDate;
import java.util.HashSet;
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
                var suppliers = xlsx.getSheet("Suppliers").getRow(0);
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
                    if (sheet.getSheetName().equals("Guide")) continue;
                    var header = sheet.getRow(0);
                    var unique = new HashSet<String>();
                    for (var cell : header) {
                        String displayed = cell.getStringCellValue();
                        assertFalse(displayed.matches(".*[a-z][A-Z].*"), sheet.getSheetName() + ": " + displayed);
                        assertTrue(unique.add(displayed), sheet.getSheetName() + ": " + displayed);
                    }
                }
            }
            var parsed = reader.read(new ByteArrayInputStream(bytes));
            assertEquals(date, parsed.getBusinessDate());
            assertEquals(2, parsed.getTemplateVersion());
            assertEquals("SUP-001", parsed.getSuppliers().getFirst().supplierKey);
            assertEquals("R-OLIVE-001", parsed.getReceptions().getFirst().externalRef);
            assertEquals("P-OIL-001", parsed.getPayments().getFirst().externalRef);
            assertEquals("S-001", parsed.getOilSales().getFirst().externalRef);
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

    @Test void generatedSampleHasVersionedPaymentReferences() throws Exception {
        var factory = new DayImportTemplateFactory();
        var workbook = new DayImportWorkbookReader().read(new ByteArrayInputStream(factory.sampleTemplate(LocalDate.of(2026,9,5))));
        assertEquals(2, workbook.getTemplateVersion());
        assertFalse(workbook.getPayments().isEmpty());
        assertEquals("P-OIL-001", workbook.getPayments().getFirst().externalRef);
        assertEquals(workbook.getBusinessDate(), workbook.getPayments().getFirst().paymentDate);
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
