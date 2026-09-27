package com.xdev.ooms.production.dayimport.service;

import org.junit.jupiter.api.Test;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class DayImportWorkbookTest {
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
