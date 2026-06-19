package com.xdev.ooms.documents.commercial;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.xdev.ooms.production.supplier.entity.Supplier;
import com.xdev.ooms.documents.layout.PdfLogoPlacement;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;
import com.xdev.ooms.production.unifieddelivery.repository.DeliveryRepository;
import com.xdev.ooms.sharedkernel.ports.CompanyProfileReadPort;
import com.xdev.ooms.sharedkernel.ports.CompanyProfileSnapshot;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Mirrors {@code PdfGeneratorFactureService.generatePdfNoteDocument} layout.
 */
@Service
public class PaymentNotePdfGeneratorService {

    private static final float PAGE_H = PageSize.A4.getHeight();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH);

    private static final Font BOLD_12 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font NORMAL_10 = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font NORMAL_9 = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font BOLD_9 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);

    private final DeliveryRepository deliveryRepository;
    private final CompanyProfileReadPort companyProfileReadPort;
    private final BillLabelResolver billLabelResolver;

    public PaymentNotePdfGeneratorService(
            DeliveryRepository deliveryRepository,
            CompanyProfileReadPort companyProfileReadPort,
            BillLabelResolver billLabelResolver) {
        this.deliveryRepository = deliveryRepository;
        this.companyProfileReadPort = companyProfileReadPort;
        this.billLabelResolver = billLabelResolver;
    }

    @Transactional(readOnly = true)
    public BillDocument generatePaymentNote(UUID deliveryId) {
        UnifiedDelivery delivery = deliveryRepository.findByIdForPdf(deliveryId)
                .orElseThrow(() -> new EntityNotFoundException("Delivery not found: " + deliveryId));
        CompanyProfileSnapshot profile = companyProfileReadPort.findCurrentTenantProfile()
                .orElseThrow(() -> new IllegalStateException("Company profile is required to generate payment note PDF"));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 0, 0, 0, 0);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();
            PdfContentByte canvas = writer.getDirectContent();

            float currentY = 10f;
            float marginLeft = 10f;
            float marginRight = 10f;
            float pageWidth = 210f;
            float logoWidth = 25f;
            float logoHeight = 25f;
            float rightX = pageWidth - 100f;
            float rowHeight = 10f;
            float lineHeight = 7f;
            float clientPadding = 4f;

            Image logo = loadLogo(profile.logoBase64(), profile.logoContentType());
            if (logo != null) {
                PdfLogoPlacement.drawCenteredInBox(canvas, logo, marginLeft, currentY, logoWidth, logoHeight, 0f);
            }

            float companyInfoX = marginLeft + 10f;
            float companyInfoYStart = currentY + 25f;
            float leftColumnMaxWidth = rightX - companyInfoX - 6f;
            float yPtr = companyInfoYStart;
            yPtr = drawWrapped(canvas, safe(profile.legalName(), PaymentNoteLabels.COMPANY_NAME), companyInfoX, yPtr,
                    leftColumnMaxWidth, lineHeight, NORMAL_9);
            yPtr = drawWrapped(canvas, safe(profile.address(), PaymentNoteLabels.ADDRESS), companyInfoX, yPtr,
                    leftColumnMaxWidth, lineHeight, NORMAL_9);
            yPtr = drawWrapped(canvas, PaymentNoteLabels.VAT + " " + safe(profile.taxId(), PaymentNoteLabels.VAT),
                    companyInfoX, yPtr, leftColumnMaxWidth, lineHeight, NORMAL_9);
            yPtr = drawWrapped(canvas, PaymentNoteLabels.MOBILE + " " + safe(profile.phone(), PaymentNoteLabels.MOBILE),
                    companyInfoX, yPtr, leftColumnMaxWidth, lineHeight, NORMAL_9);
            yPtr = drawWrapped(canvas, PaymentNoteLabels.WEBSITE + " " + safe(profile.website(), PaymentNoteLabels.WEBSITE),
                    companyInfoX, yPtr, leftColumnMaxWidth, lineHeight, NORMAL_9);

            drawText(canvas, PaymentNoteLabels.NOTE_DE_PAIEMENT, rightX, currentY + 25f, BOLD_12);
            String reference = referenceDate(delivery);
            drawText(canvas, PaymentNoteLabels.REFERENCE + " : " + reference, rightX, currentY + 32f, NORMAL_10);
            drawText(canvas, PaymentNoteLabels.DATE + " : " + LocalDateTime.now().format(DATE_FMT), rightX, currentY + 39f,
                    NORMAL_10);

            float clientBlockX = rightX;
            float clientBlockYStart = currentY + 50f;
            float maxClientWidth = pageWidth - clientBlockX - marginRight - 5f;
            List<ClientLine> clientLines = buildClientLines(delivery);
            float clientBlockHeight = 6f + clientLines.stream()
                    .mapToInt(line -> line.lines.size())
                    .sum() * lineHeight;
            float clientBlockWidth = Math.min(maxClientWidth, 95f);

            fillRect(canvas, clientBlockX, clientBlockYStart, clientBlockWidth, clientBlockHeight, new BaseColor(245, 245, 245));
            drawRect(canvas, clientBlockX, clientBlockYStart, clientBlockWidth, clientBlockHeight);
            drawText(canvas, PaymentNoteLabels.CLIENT_INFO, clientBlockX + clientPadding, clientBlockYStart + 6f, BOLD_9);

            float textY = clientBlockYStart + 12f;
            for (ClientLine clientLine : clientLines) {
                for (String line : clientLine.lines) {
                    drawText(canvas, line, clientBlockX + clientPadding, textY, NORMAL_9);
                    textY += lineHeight;
                }
            }

            currentY = clientBlockYStart + clientBlockHeight + 20f;
            currentY = drawPaymentTable(canvas, currentY, pageWidth, rowHeight, delivery);

            document.close();
            String fileName = "Note_Paiement_" + safe(delivery.getDeliveryNumber(), "inconnu") + ".pdf";
            return new BillDocument(reference, fileName, "application/pdf", out.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate payment note PDF", e);
        }
    }

    private float drawPaymentTable(PdfContentByte canvas, float currentY, float pageWidth, float rowHeight,
                                   UnifiedDelivery delivery) throws DocumentException {
        float col1Width = 35f;
        float col2Width = 35f;
        float col3Width = 30f;
        float col4Width = 35f;
        float col5Width = 35f;
        float tableWidth = col1Width + col2Width + col3Width + col4Width + col5Width;
        float tableLeft = (pageWidth - tableWidth) / 2f;

        float x = tableLeft;
        float[] widths = {col1Width, col2Width, col3Width, col4Width, col5Width};
        fillHeaderRow(canvas, x, currentY, widths, rowHeight);

        currentY += rowHeight;
        drawRect(canvas, tableLeft, currentY, col1Width, rowHeight);
        drawRect(canvas, tableLeft + col1Width, currentY, col2Width, rowHeight);
        drawRect(canvas, tableLeft + col1Width + col2Width, currentY, col3Width, rowHeight);
        drawRect(canvas, tableLeft + col1Width + col2Width + col3Width, currentY, col4Width, rowHeight);
        drawRect(canvas, tableLeft + col1Width + col2Width + col3Width + col4Width, currentY, col5Width, rowHeight);

        BigDecimal total = money(delivery.getPrice());
        BigDecimal paid = money(delivery.getPaidAmount());
        BigDecimal unpaid = resolveUnpaid(delivery, total, paid);

        drawText(canvas, "CASH", tableLeft + 2f, currentY + 6f, NORMAL_9);
        drawRightText(canvas, fmtMoney(total), tableLeft + col1Width + col2Width - 2f, currentY + 6f, NORMAL_9);
        drawRightText(canvas, fmtMoney(paid), tableLeft + col1Width + col2Width + col3Width - 2f, currentY + 6f, NORMAL_9);
        drawText(canvas, LocalDateTime.now().format(DATE_FMT),
                tableLeft + col1Width + col2Width + col3Width + 2f, currentY + 6f, NORMAL_9);
        drawRightText(canvas, fmtMoney(unpaid),
                tableLeft + col1Width + col2Width + col3Width + col4Width + col5Width - 2f, currentY + 6f, NORMAL_9);

        return currentY + rowHeight;
    }

    private void fillHeaderRow(PdfContentByte canvas, float x, float y, float[] widths, float rowHeight) {
        fillRect(canvas, x, y, widths[0], rowHeight, new BaseColor(200, 200, 200));
        x += widths[0];
        fillRect(canvas, x, y, widths[1], rowHeight, new BaseColor(200, 200, 200));
        x += widths[1];
        fillRect(canvas, x, y, widths[2], rowHeight, new BaseColor(200, 200, 200));
        x += widths[2];
        fillRect(canvas, x, y, widths[3], rowHeight, new BaseColor(200, 200, 200));
        x += widths[3];
        fillRect(canvas, x, y, widths[4], rowHeight, new BaseColor(200, 200, 200));

        float left = (210f - (widths[0] + widths[1] + widths[2] + widths[3] + widths[4])) / 2f;
        drawText(canvas, PaymentNoteLabels.PAYMENT_TYPE, left + 2f, y + 6f, BOLD_9);
        drawText(canvas, PaymentNoteLabels.TOTAL_AMOUNT, left + widths[0] + 2f, y + 6f, BOLD_9);
        drawText(canvas, PaymentNoteLabels.PAID_AMOUNT, left + widths[0] + widths[1] + 2f, y + 6f, BOLD_9);
        drawText(canvas, PaymentNoteLabels.PAYMENT_DATE, left + widths[0] + widths[1] + widths[2] + 2f, y + 6f, BOLD_9);
        drawText(canvas, PaymentNoteLabels.REMAINING_AMOUNT,
                left + widths[0] + widths[1] + widths[2] + widths[3] + 2f, y + 6f, BOLD_9);
    }

    private List<ClientLine> buildClientLines(UnifiedDelivery delivery) throws DocumentException, IOException {
        List<ClientLine> lines = new ArrayList<>();
        Supplier supplier = delivery.getSupplier();
        lines.add(wrapLine(PaymentNoteLabels.CLIENT, supplierName(supplier), 90f));
        lines.add(wrapLine(PaymentNoteLabels.PHONE, supplier == null ? "—" : safe(supplier.getPhone(), "—"), 90f));
        lines.add(wrapLine(PaymentNoteLabels.ADDRESS, supplier == null ? "—" : safe(supplier.getAddress(), "—"), 90f));
        lines.add(wrapLine(PaymentNoteLabels.OPERATION_TYPE,
                billLabelResolver.operationTypeLabel(delivery.getOperationType()), 90f));
        lines.add(wrapLine(PaymentNoteLabels.INVOICE_NUMBER, safe(delivery.getDeliveryNumber(), "—"), 90f));
        lines.add(wrapLine(PaymentNoteLabels.LOT_NUMBER, safe(delivery.getLotNumber(), "—"), 90f));
        lines.add(wrapLine(PaymentNoteLabels.REFERENCE_DATE, referenceDate(delivery), 90f));
        return lines;
    }

    private ClientLine wrapLine(String label, String value, float maxWidthMm) throws DocumentException, IOException {
        String fullText = label + " : " + (value == null ? "—" : value);
        List<String> wrapped = splitLines(fullText, maxWidthMm, 9f, false);
        return new ClientLine(wrapped);
    }

    private String referenceDate(UnifiedDelivery delivery) {
        String year = String.valueOf(LocalDateTime.now().getYear()).substring(2);
        return "N°" + safe(delivery.getDeliveryNumber(), "—") + " / " + year;
    }

    private BigDecimal resolveUnpaid(UnifiedDelivery delivery, BigDecimal total, BigDecimal paid) {
        if (delivery.getUnpaidAmount() != null && delivery.getUnpaidAmount() > 0) {
            return money(delivery.getUnpaidAmount());
        }
        BigDecimal rest = total.subtract(paid);
        return rest.signum() > 0 ? rest : BigDecimal.ZERO;
    }

    private String supplierName(Supplier supplier) {
        if (supplier == null) {
            return "—";
        }
        String name = (safe(supplier.getName()) + " " + safe(supplier.getLastname())).trim();
        return name.isBlank() ? "—" : name;
    }

    private String fmtMoney(BigDecimal amount) {
        return String.format(Locale.FRENCH, "%.2f TND", amount == null ? BigDecimal.ZERO : amount);
    }

    private BigDecimal money(Double value) {
        return value == null ? BigDecimal.ZERO : BigDecimal.valueOf(value);
    }

    private Image loadLogo(String logoBase64, String contentType) {
        if (logoBase64 == null || logoBase64.isBlank()) {
            return null;
        }
        try {
            return Image.getInstance(Base64.getDecoder().decode(logoBase64.trim()));
        } catch (Exception e) {
            return null;
        }
    }

    private float drawWrapped(PdfContentByte canvas, String text, float xMm, float yMm, float maxWidthMm,
                              float lineHeightMm, Font font) throws DocumentException, IOException {
        List<String> lines = splitLines(text, maxWidthMm, font.getSize(), font.getStyle() == Font.BOLD);
        for (int i = 0; i < lines.size(); i++) {
            drawText(canvas, lines.get(i), xMm, yMm + i * lineHeightMm, font);
        }
        return yMm + lines.size() * lineHeightMm;
    }

    private List<String> splitLines(String text, float maxWidthMm, float fontSize, boolean bold) throws DocumentException, IOException {
        BaseFont baseFont = bold
                ? BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED)
                : BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        float maxWidthPt = mmToPt(maxWidthMm);
        List<String> lines = new ArrayList<>();
        if (text == null || text.isBlank()) {
            lines.add("");
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

    private void drawText(PdfContentByte canvas, String text, float xMm, float yMm, Font font) {
        if (text == null) {
            return;
        }
        canvas.beginText();
        canvas.setFontAndSize(font.getBaseFont(), font.getSize());
        canvas.setColorFill(BaseColor.BLACK);
        canvas.showTextAligned(Element.ALIGN_LEFT, text, mmToPt(xMm), yTop(yMm), 0);
        canvas.endText();
    }

    private void drawRightText(PdfContentByte canvas, String text, float xMm, float yMm, Font font) {
        if (text == null) {
            return;
        }
        canvas.beginText();
        canvas.setFontAndSize(font.getBaseFont(), font.getSize());
        canvas.setColorFill(BaseColor.BLACK);
        canvas.showTextAligned(Element.ALIGN_RIGHT, text, mmToPt(xMm), yTop(yMm), 0);
        canvas.endText();
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

    private float mmToPt(float mm) {
        return PdfLogoPlacement.mmToPt(mm);
    }

    private float yTop(float yMmFromTop) {
        return PdfLogoPlacement.yFromTopMm(yMmFromTop);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private record ClientLine(List<String> lines) {
    }

    private static final class PaymentNoteLabels {
        static final String NOTE_DE_PAIEMENT = "Note de paiment";
        static final String REFERENCE = "Référence";
        static final String DATE = "Date";
        static final String COMPANY_NAME = "Nom de l'entreprise";
        static final String ADDRESS = "Adresse";
        static final String VAT = "Numéro TVA";
        static final String MOBILE = "N° tél";
        static final String WEBSITE = "Site web";
        static final String CLIENT_INFO = "Client";
        static final String CLIENT = "Client";
        static final String PHONE = "Téléphone";
        static final String OPERATION_TYPE = "Type d'opération";
        static final String INVOICE_NUMBER = "Numéro de facture";
        static final String LOT_NUMBER = "N° Lot";
        static final String REFERENCE_DATE = "Date de référence";
        static final String PAYMENT_TYPE = "Payment Type";
        static final String TOTAL_AMOUNT = "Montant total";
        static final String PAID_AMOUNT = "Paid Amount";
        static final String PAYMENT_DATE = "Payment Date";
        static final String REMAINING_AMOUNT = "Remaining Amount";

        private PaymentNoteLabels() {
        }
    }
}
