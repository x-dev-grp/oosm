package com.xdev.ooms.documents.form;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.xdev.ooms.documents.form.dto.FormPdfConfigDto;
import com.xdev.ooms.documents.form.dto.FormPdfDocument;
import com.xdev.ooms.documents.form.dto.FormPdfFieldDto;
import com.xdev.ooms.documents.form.dto.FormPdfFooterDto;
import com.xdev.ooms.documents.layout.PdfLogoPlacement;
import com.xdev.ooms.sharedkernel.ports.CompanyProfileReadPort;
import com.xdev.ooms.sharedkernel.ports.CompanyProfileSnapshot;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * iText implementation mirroring {@code pdf-generator.service.ts} form layout.
 */
@Service
public class FormPdfGeneratorService {

    private static final float PAGE_H = PageSize.A4.getHeight();
    private static final float PAGE_W = PageSize.A4.getWidth();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH);

    private static final Font TITLE_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font TITLE_ITALIC = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10);
    private static final Font NORMAL_9 = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font BOLD_9 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    private static final Font BOLD_10 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font NORMAL_10 = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font NORMAL_12 = FontFactory.getFont(FontFactory.HELVETICA, 12);
    private static final Font FOOTER_PLACEHOLDER = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY);

    private final CompanyProfileReadPort companyProfileReadPort;

    public FormPdfGeneratorService(CompanyProfileReadPort companyProfileReadPort) {
        this.companyProfileReadPort = companyProfileReadPort;
    }

    public FormPdfDocument generate(FormPdfConfigDto config) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 0, 0, 0, 0);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();
            PdfContentByte canvas = writer.getDirectContent();

            float currentY = 10f;
            float marginLeft = 10f;
            float logoWidth = 30f;
            float logoHeight = 20f;
            float headerHeight = 20f;
            float pageWidth = 210f;

            drawLogoBox(canvas, marginLeft, currentY, logoWidth, logoHeight);

            float centerX = marginLeft + logoWidth;
            float centerWidth = 100f;
            drawRect(canvas, centerX, currentY, centerWidth, headerHeight);
            drawCenteredText(canvas, FormPdfLabels.FORM, centerX, currentY + 7f, centerWidth, TITLE_BOLD);
            drawCenteredText(canvas, safe(config.getTitle()), centerX, currentY + 14f, centerWidth, TITLE_ITALIC);

            float rightX = centerX + centerWidth;
            float rowHeight = 5f;
            float infoWidth = pageWidth - rightX - marginLeft;
            String documentDate = hasText(config.getDate()) ? config.getDate() : LocalDate.now().format(DATE_FMT);

            String[][] infoRows = {
                    {FormPdfLabels.REFERENCE, safe(config.getReference(), "—")},
                    {FormPdfLabels.REVISION, safe(config.getRevision(), "....")},
                    {FormPdfLabels.DATE, documentDate},
                    {FormPdfLabels.PAGE, "1/1"}
            };
            for (int index = 0; index < infoRows.length; index++) {
                float y = currentY + index * rowHeight;
                drawRect(canvas, rightX, y, infoWidth, rowHeight);
                drawText(canvas, infoRows[index][0] + " : " + infoRows[index][1], rightX + 2f, y + 4f, NORMAL_9);
            }

            currentY += headerHeight + 10f;

            if (hasText(config.getNumber())) {
                drawCenteredText(canvas, FormPdfLabels.NUMBER_PLACEHOLDER + config.getNumber(),
                        marginLeft, currentY, pageWidth - marginLeft * 2, BOLD_10);
                currentY += 15f;
            }

            List<FormPdfFieldDto> generalInfo = filterFields(config.getGeneralInfo());
            if (!generalInfo.isEmpty()) {
                for (FormPdfFieldDto info : generalInfo) {
                    drawText(canvas, info.getLabel() + " : " + info.getValue(), marginLeft, currentY, NORMAL_12);
                    currentY += 10f;
                }
                currentY += 15f;
            }

            List<FormPdfFieldDto> fields = filterFields(config.getFields());
            if (!fields.isEmpty()) {
                currentY = drawHorizontalFieldsTable(canvas, marginLeft, currentY, fields);
                currentY += 10f;
            }

            if (config.getFooterInfo() != null && !config.getFooterInfo().isEmpty()) {
                drawFooter(canvas, marginLeft, pageWidth, config.getFooterInfo());
            }

            document.close();
            String fileName = hasText(config.getFileName()) ? config.getFileName() : "document.pdf";
            return new FormPdfDocument(fileName, out.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate form PDF", e);
        }
    }

    private float drawHorizontalFieldsTable(PdfContentByte canvas, float tableLeft, float currentY,
                                            List<FormPdfFieldDto> fields) throws DocumentException, IOException {
        float tableWidth = 190f;
        int colCount = fields.size();
        float colWidth = tableWidth / colCount;
        float baseFontSize = 9f;

        List<List<String>> splitLabels = new ArrayList<>();
        int maxLines = 1;
        for (FormPdfFieldDto field : fields) {
            List<String> lines = splitLines(field.getLabel(), colWidth - 4f, baseFontSize, true);
            splitLabels.add(lines);
            maxLines = Math.max(maxLines, lines.size());
        }
        float labelRowHeight = maxLines * 5f + 2f;

        for (int i = 0; i < colCount; i++) {
            float x = tableLeft + i * colWidth;
            fillRect(canvas, x, currentY, colWidth, labelRowHeight, new BaseColor(200, 200, 200));
            List<String> lines = splitLabels.get(i);
            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                drawText(canvas, lines.get(lineIndex), x + 2f, currentY + 6f + lineIndex * 5f, BOLD_9);
            }
        }

        currentY += labelRowHeight;
        List<List<String>> splitValues = new ArrayList<>();
        int maxValueLines = 1;
        for (FormPdfFieldDto field : fields) {
            List<String> lines = null;
            try {
                lines = splitLines(field.getValue(), colWidth - 4f, baseFontSize, false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            splitValues.add(lines);
            maxValueLines = Math.max(maxValueLines, lines.size());
        }
        float dataRowHeight = maxValueLines * 5f + 2f;

        for (int i = 0; i < colCount; i++) {
            float x = tableLeft + i * colWidth;
            drawRect(canvas, x, currentY, colWidth, dataRowHeight);
            List<String> lines = splitValues.get(i);
            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                drawText(canvas, lines.get(lineIndex), x + 2f, currentY + 6f + lineIndex * 5f, NORMAL_9);
            }
        }
        return currentY + dataRowHeight + 5f;
    }

    private void drawFooter(PdfContentByte canvas, float marginLeft, float pageWidth,
                            List<FormPdfFooterDto> footerInfo) {
        float separatorY = 270f;
        canvas.setColorStroke(BaseColor.BLACK);
        canvas.moveTo(mmToPt(10f), yTop(separatorY));
        canvas.lineTo(mmToPt(200f), yTop(separatorY));
        canvas.stroke();

        float footerY = 280f;
        float marginRight = 10f;
        float usableWidth = pageWidth - marginLeft - marginRight;
        int maxItemsPerRow = 4;
        int itemCount = Math.min(footerInfo.size(), maxItemsPerRow);
        float spacing = usableWidth / itemCount;
        float footerLabelWidth = 40f;

        for (int index = 0; index < itemCount; index++) {
            FormPdfFooterDto footerItem = footerInfo.get(index);
            float x = marginLeft + index * spacing;
            if (!hasText(footerItem.getLabel())) {
                continue;
            }
            drawText(canvas, footerItem.getLabel() + " :", x, footerY, NORMAL_10);
            if (hasText(footerItem.getPlaceholder())) {
                drawText(canvas, footerItem.getPlaceholder(), x + footerLabelWidth, footerY, FOOTER_PLACEHOLDER);
            }
        }
    }

    private void drawLogoBox(PdfContentByte canvas, float xMm, float yMm, float widthMm, float heightMm) {
        drawRect(canvas, xMm, yMm, widthMm, heightMm);
        Optional<CompanyProfileSnapshot> profile = companyProfileReadPort.findCurrentTenantProfile();
        if (profile.isEmpty()) {
            return;
        }
        CompanyProfileSnapshot snapshot = profile.get();
        Image logo = loadLogo(snapshot.logoBase64(), snapshot.logoContentType());
        if (logo == null) {
            return;
        }
        try {
            PdfLogoPlacement.drawCenteredInBox(canvas, logo, xMm, yMm, widthMm, heightMm, 1f);
        } catch (DocumentException e) {
            // skip logo on failure
        }
    }

    private Image loadLogo(String logoBase64, String contentType) {
        if (!hasText(logoBase64)) {
            return null;
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(logoBase64.trim());
            return Image.getInstance(bytes);
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<FormPdfFieldDto> filterFields(List<FormPdfFieldDto> fields) {
        if (fields == null) {
            return List.of();
        }
        return fields.stream()
                .filter(f -> hasText(f.getLabel()) && hasText(f.getValue()))
                .toList();
    }

    private List<String> splitLines(String text, float maxWidthMm, float fontSize, boolean bold) throws DocumentException, IOException {
        BaseFont baseFont = bold ? BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED)
                : BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        float maxWidthPt = mmToPt(maxWidthMm);
        List<String> lines = new ArrayList<>();
        if (!hasText(text)) {
            lines.add("—");
            return lines;
        }
        int start = 0;
        while (start < text.length()) {
            int end = text.length();
            while (end > start && baseFont.getWidthPoint(text.substring(start, end), fontSize) > maxWidthPt) {
                end--;
            }
            if (end == start) {
                end = Math.min(start + 1, text.length());
            }
            lines.add(text.substring(start, end));
            start = end;
        }
        return lines;
    }

    private void drawRect(PdfContentByte canvas, float xMm, float yMm, float widthMm, float heightMm) {
        canvas.setColorStroke(BaseColor.BLACK);
        canvas.rectangle(mmToPt(xMm), yTop(yMm + heightMm), mmToPt(widthMm), mmToPt(heightMm));
        canvas.stroke();
    }

    private void fillRect(PdfContentByte canvas, float xMm, float yMm, float widthMm, float heightMm, BaseColor fill) {
        canvas.setColorFill(fill);
        canvas.setColorStroke(BaseColor.BLACK);
        canvas.rectangle(mmToPt(xMm), yTop(yMm + heightMm), mmToPt(widthMm), mmToPt(heightMm));
        canvas.fillStroke();
    }

    private void drawText(PdfContentByte canvas, String text, float xMm, float yMm, Font font) {
        if (!hasText(text)) {
            return;
        }
        canvas.beginText();
        canvas.setFontAndSize(font.getBaseFont(), font.getSize());
        canvas.setColorFill(BaseColor.BLACK);
        canvas.showTextAligned(Element.ALIGN_LEFT, text, mmToPt(xMm), yTop(yMm), 0);
        canvas.endText();
    }

    private void drawCenteredText(PdfContentByte canvas, String text, float xMm, float yMm, float widthMm, Font font) {
        if (!hasText(text)) {
            return;
        }
        canvas.beginText();
        canvas.setFontAndSize(font.getBaseFont(), font.getSize());
        canvas.setColorFill(BaseColor.BLACK);
        canvas.showTextAligned(Element.ALIGN_CENTER, text, mmToPt(xMm + widthMm / 2f), yTop(yMm), 0);
        canvas.endText();
    }

    private float mmToPt(float mm) {
        return PdfLogoPlacement.mmToPt(mm);
    }

    private float yTop(float yMmFromTop) {
        return PdfLogoPlacement.yFromTopMm(yMmFromTop);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty()
                && !"undefined".equalsIgnoreCase(value.trim())
                && !"null".equalsIgnoreCase(value.trim());
    }

    private String safe(String value) {
        return safe(value, "—");
    }

    private String safe(String value, String fallback) {
        return hasText(value) ? value.trim() : fallback;
    }
}
