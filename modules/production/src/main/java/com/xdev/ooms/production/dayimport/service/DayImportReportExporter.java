package com.xdev.ooms.production.dayimport.service;

import com.xdev.ooms.production.dayimport.dto.DayImportReportDto;
import com.xdev.ooms.production.dayimport.dto.ImportRowResultDto;
import com.xdev.ooms.production.dayimport.dto.ImportRowStatus;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Export dry-run / commit reports. Styling matches {@link DayImportTemplateFactory} (OOSM primary #4680FF).
 */
@Component
public class DayImportReportExporter {

    private static final byte[] PRIMARY = new byte[]{(byte) 0x46, (byte) 0x80, (byte) 0xFF};
    private static final byte[] OLIVE = new byte[]{(byte) 0x3D, (byte) 0x5A, (byte) 0x2C};
    private static final byte[] WHITE = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    private static final byte[] TEXT = new byte[]{(byte) 0x3E, (byte) 0x48, (byte) 0x53};
    private static final byte[] GREY_200 = new byte[]{(byte) 0xF3, (byte) 0xF5, (byte) 0xF7};
    private static final byte[] GREY_BORDER = new byte[]{(byte) 0xDB, (byte) 0xE0, (byte) 0xE5};
    private static final byte[] OK_BG = new byte[]{(byte) 0xE8, (byte) 0xF5, (byte) 0xE9};
    private static final byte[] ERR_BG = new byte[]{(byte) 0xFF, (byte) 0xEB, (byte) 0xEE};
    private static final byte[] SKIP_BG = new byte[]{(byte) 0xFF, (byte) 0xF8, (byte) 0xE1};

    public byte[] toCsv(DayImportReportDto report) {
        StringBuilder sb = new StringBuilder();
        sb.append("sheet,rowNumber,businessKey,status,message,stockDelta\n");
        for (ImportRowResultDto row : report.getRows()) {
            sb.append(csv(row.getSheet())).append(',')
                    .append(row.getRowNumber()).append(',')
                    .append(csv(row.getBusinessKey())).append(',')
                    .append(row.getStatus() != null ? row.getStatus().name() : "").append(',')
                    .append(csv(row.getMessage())).append(',')
                    .append(row.getStockDelta() != null ? row.getStockDelta() : "")
                    .append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] toXlsx(DayImportReportDto report) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Styles styles = new Styles(wb);

            Sheet summary = wb.createSheet("Summary");
            summary.setColumnWidth(0, 18 * 256);
            summary.setColumnWidth(1, 28 * 256);

            Row title = summary.createRow(0);
            title.setHeightInPoints(24);
            title.createCell(0).setCellValue("OOSM — Day import report");
            title.getCell(0).setCellStyle(styles.title);
            summary.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

            writePair(summary, styles, 2, "businessDate",
                    report.getBusinessDate() != null ? report.getBusinessDate().toString() : "");
            writePair(summary, styles, 3, "canCommit", String.valueOf(report.isCanCommit()));
            writePair(summary, styles, 4, "validCount", String.valueOf(report.getValidCount()));
            writePair(summary, styles, 5, "invalidCount", String.valueOf(report.getInvalidCount()));

            Sheet sheet = wb.createSheet("Report");
            sheet.createFreezePane(0, 1);
            String[] headers = {"sheet", "rowNumber", "businessKey", "status", "message", "stockDelta"};
            Row header = sheet.createRow(0);
            header.setHeightInPoints(22);
            for (int c = 0; c < headers.length; c++) {
                header.createCell(c).setCellValue(headers[c]);
                header.getCell(c).setCellStyle(styles.header);
                sheet.setColumnWidth(c, c == 4 ? 48 * 256 : 16 * 256);
            }

            int i = 1;
            for (ImportRowResultDto row : report.getRows()) {
                Row r = sheet.createRow(i++);
                XSSFCellStyle rowStyle = statusStyle(styles, row.getStatus());
                r.createCell(0).setCellValue(empty(row.getSheet()));
                r.createCell(1).setCellValue(row.getRowNumber());
                r.createCell(2).setCellValue(empty(row.getBusinessKey()));
                r.createCell(3).setCellValue(row.getStatus() != null ? row.getStatus().name() : "");
                r.createCell(4).setCellValue(empty(row.getMessage()));
                if (row.getStockDelta() != null) {
                    r.createCell(5).setCellValue(row.getStockDelta());
                } else {
                    r.createCell(5).setCellValue("");
                }
                for (int c = 0; c <= 5; c++) {
                    r.getCell(c).setCellStyle(rowStyle);
                }
            }

            wb.setActiveSheet(0);
            wb.write(out);
            return out.toByteArray();
        }
    }

    private void writePair(Sheet sheet, Styles styles, int rowIdx, String key, String value) {
        Row row = sheet.createRow(rowIdx);
        row.createCell(0).setCellValue(key);
        row.getCell(0).setCellStyle(styles.label);
        row.createCell(1).setCellValue(value);
        row.getCell(1).setCellStyle(styles.data);
    }

    private XSSFCellStyle statusStyle(Styles styles, ImportRowStatus status) {
        if (status == null) {
            return styles.data;
        }
        return switch (status) {
            case ERROR -> styles.error;
            case SKIP_DUPLICATE, WARNING -> styles.skip;
            case CREATE, LINK_EXISTING -> styles.ok;
        };
    }

    private String csv(String value) {
        return "\"" + empty(value).replace("\"", "\"\"") + "\"";
    }

    private String empty(String v) {
        return v == null ? "" : v;
    }

    private static final class Styles {
        final XSSFCellStyle title;
        final XSSFCellStyle header;
        final XSSFCellStyle label;
        final XSSFCellStyle data;
        final XSSFCellStyle ok;
        final XSSFCellStyle error;
        final XSSFCellStyle skip;

        Styles(XSSFWorkbook wb) {
            DefaultIndexedColorMap colors = new DefaultIndexedColorMap();

            XSSFFont titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleFont.setColor(new XSSFColor(WHITE, colors));

            XSSFFont headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setColor(new XSSFColor(WHITE, colors));

            XSSFFont labelFont = wb.createFont();
            labelFont.setBold(true);
            labelFont.setFontHeightInPoints((short) 10);
            labelFont.setColor(new XSSFColor(OLIVE, colors));

            XSSFFont bodyFont = wb.createFont();
            bodyFont.setFontHeightInPoints((short) 10);
            bodyFont.setColor(new XSSFColor(TEXT, colors));

            title = wb.createCellStyle();
            title.setFont(titleFont);
            title.setFillForegroundColor(new XSSFColor(OLIVE, colors));
            title.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            title.setVerticalAlignment(VerticalAlignment.CENTER);

            header = wb.createCellStyle();
            header.setFont(headerFont);
            header.setFillForegroundColor(new XSSFColor(PRIMARY, colors));
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            header.setAlignment(HorizontalAlignment.CENTER);
            header.setVerticalAlignment(VerticalAlignment.CENTER);
            border(header, colors);

            label = wb.createCellStyle();
            label.setFont(labelFont);
            label.setFillForegroundColor(new XSSFColor(GREY_200, colors));
            label.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            border(label, colors);

            data = wb.createCellStyle();
            data.setFont(bodyFont);
            border(data, colors);

            ok = wb.createCellStyle();
            ok.cloneStyleFrom(data);
            ok.setFillForegroundColor(new XSSFColor(OK_BG, colors));
            ok.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            error = wb.createCellStyle();
            error.cloneStyleFrom(data);
            error.setFillForegroundColor(new XSSFColor(ERR_BG, colors));
            error.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            skip = wb.createCellStyle();
            skip.cloneStyleFrom(data);
            skip.setFillForegroundColor(new XSSFColor(SKIP_BG, colors));
            skip.setFillPattern(FillPatternType.SOLID_FOREGROUND);
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
