package com.xdev.ooms.documents.commercial;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.xdev.ooms.documents.commercial.dto.BillBankInfoDto;
import com.xdev.ooms.documents.commercial.dto.BillFooterContactDto;
import com.xdev.ooms.documents.commercial.dto.BillGenerationRequest;
import com.xdev.ooms.documents.commercial.dto.BillLineDto;
import com.xdev.ooms.documents.commercial.dto.BillLogisticsDto;
import com.xdev.ooms.documents.commercial.dto.BillPartyDto;
import com.xdev.ooms.documents.commercial.dto.BillTotalsDto;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@Service
public class BillPdfGeneratorService {

    private static final float MARGIN_PT = 34f;
    private static final DateTimeFormatter INVOICE_DATE =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.FRENCH);
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font META_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font TOTAL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

    private final TunisiaBillComplianceValidator validator;

    public BillPdfGeneratorService(TunisiaBillComplianceValidator validator) {
        this.validator = validator;
    }

    public BillDocument generate(BillGenerationRequest request) {
        validator.validate(request);
        BillTotalsDto totals = calculateTotals(request);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, MARGIN_PT, MARGIN_PT, MARGIN_PT, MARGIN_PT);
            PdfWriter.getInstance(document, out);
            document.open();

            addCommercialHeader(document, request);
            addClientBox(document, request.getClient());
            addCommercialLines(document, request, totals);
            addTaxSummary(document, request, totals);
            addLogisticsAndBank(document, request);
            addPaymentTerms(document, request);
            addTaxLegalMention(document, request);
            addFooterContact(document, request);

            document.close();
            String fileName = "facture-" + safeFileName(request.getInvoiceNumber()) + ".pdf";
            return new BillDocument(request.getInvoiceNumber(), fileName, "application/pdf", out.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate bill PDF", e);
        }
    }

    public BillTotalsDto calculateTotals(BillGenerationRequest request) {
        return BillVatCalculator.calculateTotals(resolveVatMode(request), request.getLines());
    }

    private BillVatMode resolveVatMode(BillGenerationRequest request) {
        return request.getVatMode() == null ? BillVatMode.STANDARD : request.getVatMode();
    }

    private void addCommercialHeader(Document document, BillGenerationRequest request) throws DocumentException {
        BillPartyDto issuer = request.getIssuer();
        String currency = normalizeCurrency(request.getCurrency());

        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{52f, 48f});
        header.setSpacingAfter(8f);

        PdfPCell left = new PdfPCell();
        left.setBorder(Rectangle.NO_BORDER);
        left.setPadding(0);
        left.addElement(buildIssuerBlock(issuer, request));
        header.addCell(left);

        PdfPCell right = new PdfPCell();
        right.setBorder(Rectangle.NO_BORDER);
        right.setPadding(0);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        right.addElement(buildMetaBlock(request, currency));
        header.addCell(right);

        document.add(header);
    }

    private PdfPTable buildIssuerBlock(BillPartyDto issuer, BillGenerationRequest request) throws DocumentException {
        PdfPTable block = new PdfPTable(1);
        block.setWidthPercentage(100);

        Image logo = loadLogo(request);
        if (logo != null) {
            logo.scaleToFit(108f, 108f);
            PdfPCell logoCell = new PdfPCell(logo, false);
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            logoCell.setPadding(4f);
            logoCell.setFixedHeight(112f);
            block.addCell(logoCell);
        }

        if (issuer != null && hasText(issuer.getDisplayName())) {
            block.addCell(noBorderCell(issuer.getDisplayName(), BOLD_FONT));
        }
        if (issuer != null && hasText(issuer.getAddress())) {
            block.addCell(noBorderCell(issuer.getAddress(), NORMAL_FONT));
        }
        if (issuer != null && hasText(issuer.getTaxRegistrationNumber())) {
            block.addCell(noBorderCell("Matricule fiscal: " + issuer.getTaxRegistrationNumber(), NORMAL_FONT));
        }
        if (issuer != null && hasText(issuer.getPhone())) {
            block.addCell(noBorderCell("N° tél " + issuer.getPhone(), NORMAL_FONT));
        }
        String web = issuer == null ? null : firstNonBlank(issuer.getWebsite(), issuer.getEmail());
        if (hasText(web)) {
            block.addCell(noBorderCell(web, NORMAL_FONT));
        }
        return block;
    }

    private PdfPTable buildMetaBlock(BillGenerationRequest request, String currency) {
        PdfPTable block = new PdfPTable(1);
        block.setWidthPercentage(100);

        String title = hasText(request.getTitle()) ? request.getTitle() : "Facture commerciale";
        PdfPCell titleCell = noBorderCell(title, TITLE_FONT);
        titleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        block.addCell(titleCell);

        PdfPCell numberCell = noBorderCell(valueOrDefault(request.getInvoiceNumber(), ""), META_FONT);
        numberCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        block.addCell(numberCell);

        if (request.getOperationDate() != null) {
            PdfPCell dateCell = noBorderCell(request.getOperationDate().format(INVOICE_DATE), META_FONT);
            dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            block.addCell(dateCell);
        }

        if (hasText(request.getConditions())) {
            PdfPCell condCell = noBorderCell("Conditions: " + request.getConditions(), META_FONT);
            condCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            block.addCell(condCell);
        }

        if (request.isElectronicInvoice()) {
            PdfPCell ttnCell = noBorderCell("Réf. TTN: " + valueOrDefault(request.getTtnReference(), ""), META_FONT);
            ttnCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            block.addCell(ttnCell);
        }

        return block;
    }

    private void addClientBox(Document document, BillPartyDto client) throws DocumentException {
        List<String> lines = clientLines(client);
        if (lines.isEmpty()) {
            return;
        }

        Paragraph content = new Paragraph();
        content.setLeading(12f);
        for (int i = 0; i < lines.size(); i++) {
            content.add(new Phrase(lines.get(i) + (i < lines.size() - 1 ? "\n" : ""), NORMAL_FONT));
        }

        PdfPCell cell = new PdfPCell(content);
        cell.setPadding(8f);
        cell.setBorder(Rectangle.BOX);

        PdfPTable box = new PdfPTable(1);
        box.setWidthPercentage(48f);
        box.setHorizontalAlignment(Element.ALIGN_LEFT);
        box.addCell(cell);
        box.setSpacingAfter(10f);
        document.add(box);
    }

    private void addCommercialLines(Document document, BillGenerationRequest request, BillTotalsDto totals)
            throws DocumentException {
        String currency = normalizeCurrency(request.getCurrency());
        BillVatMode vatMode = resolveVatMode(request);

        if (vatMode == BillVatMode.NONE) {
            addNoVatLines(document, request, currency);
            return;
        }

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3.2f, 1.2f, 0.9f, 1.2f, 0.8f, 1.2f, 1.2f});
        table.setSpacingAfter(4f);

        String unitPriceHeader = vatMode == BillVatMode.INCLUSIVE
                ? "Prix unitaire TTC " + currency + resolvePriceUnitSuffix(request.getLines())
                : "Prix unitaire HT " + currency + resolvePriceUnitSuffix(request.getLines());

        addHeaderCell(table, "Description");
        addHeaderCell(table, unitPriceHeader);
        addHeaderCell(table, "Quantité");
        addHeaderCell(table, "Montant HT " + currency);
        addHeaderCell(table, "TVA %");
        addHeaderCell(table, "Montant TVA " + currency);
        addHeaderCell(table, "Total TTC " + currency);

        for (BillLineDto line : request.getLines()) {
            BillVatCalculator.LineAmounts amounts = BillVatCalculator.lineAmounts(vatMode, line);
            BigDecimal displayedUnitPrice = vatMode == BillVatMode.INCLUSIVE
                    ? line.getUnitPriceExcludingVat()
                    : amounts.unitPriceExcludingVat();

            addBodyCell(table, valueOrDefault(line.getDesignation(), ""), Element.ALIGN_LEFT);
            addBodyCell(table, formatMoney(displayedUnitPrice, currency), Element.ALIGN_CENTER);
            addBodyCell(table, formatQtyWithUnit(line.getQuantity(), line.getUnit()), Element.ALIGN_CENTER);
            addBodyCell(table, formatMoney(amounts.lineHt(), currency), Element.ALIGN_CENTER);
            addBodyCell(table, formatQty(TunisiaVatDefaults.resolveRate(line.getVatRatePercent())), Element.ALIGN_CENTER);
            addBodyCell(table, formatMoney(amounts.lineVat(), currency), Element.ALIGN_CENTER);
            addBodyCell(table, formatMoney(amounts.lineTtc(), currency), Element.ALIGN_CENTER);
        }

        document.add(table);
    }

    private void addNoVatLines(Document document, BillGenerationRequest request, String currency)
            throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3.6f, 1.4f, 1.0f, 1.4f});
        table.setSpacingAfter(4f);

        String unitPriceHeader = "Prix unitaire " + currency + resolvePriceUnitSuffix(request.getLines());

        addHeaderCell(table, "Description");
        addHeaderCell(table, unitPriceHeader);
        addHeaderCell(table, "Quantité");
        addHeaderCell(table, "Montant " + currency);

        for (BillLineDto line : request.getLines()) {
            BillVatCalculator.LineAmounts amounts = BillVatCalculator.lineAmounts(BillVatMode.NONE, line);
            addBodyCell(table, valueOrDefault(line.getDesignation(), ""), Element.ALIGN_LEFT);
            addBodyCell(table, formatMoney(line.getUnitPriceExcludingVat(), currency), Element.ALIGN_CENTER);
            addBodyCell(table, formatQtyWithUnit(line.getQuantity(), line.getUnit()), Element.ALIGN_CENTER);
            addBodyCell(table, formatMoney(amounts.lineTtc(), currency), Element.ALIGN_CENTER);
        }

        document.add(table);
    }

    private void addTaxSummary(Document document, BillGenerationRequest request, BillTotalsDto totals)
            throws DocumentException {
        String currency = normalizeCurrency(request.getCurrency());
        BillVatMode vatMode = resolveVatMode(request);

        PdfPTable summary = new PdfPTable(2);
        summary.setWidthPercentage(42f);
        summary.setHorizontalAlignment(Element.ALIGN_RIGHT);
        summary.setSpacingAfter(8f);

        if (vatMode == BillVatMode.NONE) {
            addSummaryRow(summary, "Total " + currency, formatMoney(totals.getTotalIncludingVat(), currency));
            document.add(summary);
            return;
        }

        addSummaryRow(summary, "Total HT " + currency, formatMoney(totals.getSubtotalExcludingVat(), currency));
        addSummaryRow(summary, "Total TVA " + currency, formatMoney(totals.getVatAmount(), currency));
        if (request.getSuspendedVatAmount() != null && request.getSuspendedVatAmount().signum() > 0) {
            addSummaryRow(summary, "Taxe suspendue " + currency,
                    formatMoney(request.getSuspendedVatAmount(), currency));
        }
        addSummaryRow(summary, "Total TTC " + currency, formatMoney(totals.getTotalIncludingVat(), currency));

        document.add(summary);
    }

    private void addTaxLegalMention(Document document, BillGenerationRequest request) throws DocumentException {
        if (resolveVatMode(request) == BillVatMode.NONE) {
            return;
        }
        String mention = hasText(request.getTaxLegalMention())
                ? request.getTaxLegalMention()
                : resolveVatMode(request) == BillVatMode.INCLUSIVE
                ? TunisiaVatDefaults.INCLUSIVE_LEGAL_MENTION
                : TunisiaVatDefaults.LEGAL_MENTION;
        Paragraph legal = new Paragraph(mention, NORMAL_FONT);
        legal.setLeading(11f);
        legal.setSpacingBefore(4f);
        legal.setSpacingAfter(6f);
        document.add(legal);
    }

    private void addSummaryRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, BOLD_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(3f);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(3f);
        table.addCell(valueCell);
    }

    private void addLogisticsAndBank(Document document, BillGenerationRequest request) throws DocumentException {
        BillLogisticsDto logistics = request.getLogistics();
        BillBankInfoDto bank = request.getBankInfo();
        boolean hasLogistics = logistics != null && (
                hasText(logistics.getGrossWeight())
                        || hasText(logistics.getNetWeight())
                        || hasText(logistics.getPackages())
                        || hasText(logistics.getIncoterm())
                        || hasText(logistics.getDeliveryAddress()));
        boolean hasBank = bank != null && (
                hasText(bank.getBankName()) || hasText(bank.getIban()) || hasText(bank.getSwiftCode()));

        if (!hasLogistics && !hasBank) {
            return;
        }

        PdfPTable row = new PdfPTable(2);
        row.setWidthPercentage(100);
        row.setWidths(new float[]{52f, 48f});
        row.setSpacingAfter(6f);

        PdfPCell left = new PdfPCell();
        left.setBorder(Rectangle.NO_BORDER);
        left.setPadding(0);
        if (hasLogistics) {
            left.addElement(logisticsParagraph(logistics));
        }
        row.addCell(left);

        PdfPCell right = new PdfPCell();
        right.setBorder(Rectangle.NO_BORDER);
        right.setPadding(0);
        if (hasBank) {
            right.addElement(bankParagraph(bank));
        }
        row.addCell(right);

        document.add(row);
    }

    private Paragraph logisticsParagraph(BillLogisticsDto logistics) {
        Paragraph paragraph = new Paragraph();
        paragraph.setLeading(12f);
        if (hasText(logistics.getGrossWeight())) {
            paragraph.add(new Phrase("Poids Brut total: " + logistics.getGrossWeight() + "\n", NORMAL_FONT));
        }
        if (hasText(logistics.getNetWeight())) {
            paragraph.add(new Phrase("Poids Net total: " + logistics.getNetWeight() + "\n", NORMAL_FONT));
        }
        if (hasText(logistics.getPackages())) {
            paragraph.add(new Phrase("Nombre de colis: " + logistics.getPackages() + "\n", NORMAL_FONT));
        }
        if (hasText(logistics.getIncoterm())) {
            paragraph.add(new Phrase("Incoterm: " + logistics.getIncoterm() + "\n", NORMAL_FONT));
        }
        if (hasText(logistics.getDeliveryAddress())) {
            paragraph.add(new Phrase("Adresse de livraison: " + logistics.getDeliveryAddress() + "\n", NORMAL_FONT));
        }
        return paragraph;
    }

    private Paragraph bankParagraph(BillBankInfoDto bank) {
        Paragraph paragraph = new Paragraph("Coordonnées bancaires\n", BOLD_FONT);
        paragraph.setLeading(12f);
        if (hasText(bank.getBankName())) {
            paragraph.add(new Phrase("BANQUE: " + bank.getBankName() + "\n", NORMAL_FONT));
        }
        if (hasText(bank.getIban())) {
            paragraph.add(new Phrase("IBAN: " + bank.getIban() + "\n", NORMAL_FONT));
        }
        if (hasText(bank.getSwiftCode())) {
            paragraph.add(new Phrase("CODE SWIFT: " + bank.getSwiftCode() + "\n", NORMAL_FONT));
        }
        return paragraph;
    }

    private void addPaymentTerms(Document document, BillGenerationRequest request) throws DocumentException {
        List<String> terms = new ArrayList<>(request.getPaymentTerms() == null ? List.of() : request.getPaymentTerms());

        if (hasText(request.getPaymentMethod())) {
            terms.add(0, "Mode de paiement: " + request.getPaymentMethod());
        }
        if (hasText(request.getNotes())) {
            terms.add(request.getNotes());
        }
        if (request.isElectronicInvoice() && hasText(request.getIssuerElectronicSeal())) {
            terms.add("Sceau électronique: " + request.getIssuerElectronicSeal());
        }

        if (terms.isEmpty()) {
            return;
        }

        document.add(new Paragraph("Modalités de paiement", BOLD_FONT));
        Paragraph body = new Paragraph();
        body.setLeading(12f);
        for (String term : terms) {
            if (hasText(term)) {
                body.add(new Phrase("• " + term + "\n", NORMAL_FONT));
            }
        }
        body.setSpacingAfter(8f);
        document.add(body);
    }

    private void addFooterContact(Document document, BillGenerationRequest request) throws DocumentException {
        BillFooterContactDto footer = request.getFooterContact();
        BillPartyDto issuer = request.getIssuer();

        String company = footer != null ? footer.getCompanyName() : null;
        if (!hasText(company) && issuer != null) {
            company = issuer.getDisplayName();
        }
        String name = footer != null ? footer.getName() : null;
        String phone = footer != null ? footer.getPhone() : null;
        if (!hasText(phone) && issuer != null) {
            phone = issuer.getPhone();
        }

        if (!hasText(company) && !hasText(name) && !hasText(phone)) {
            return;
        }

        document.add(new Paragraph(" "));
        Paragraph footerBlock = new Paragraph();
        footerBlock.setLeading(12f);
        if (hasText(company)) {
            footerBlock.add(new Phrase(company + "\n", NORMAL_FONT));
        }
        if (hasText(name)) {
            footerBlock.add(new Phrase(name + "\n", NORMAL_FONT));
        }
        if (hasText(phone)) {
            footerBlock.add(new Phrase("N° tél " + phone + "\n", NORMAL_FONT));
        }
        document.add(footerBlock);
    }

    private List<String> clientLines(BillPartyDto client) {
        List<String> lines = new ArrayList<>();
        if (client == null) {
            return lines;
        }
        if (hasText(client.getDisplayName())) {
            lines.add(client.getDisplayName());
        }
        if (hasText(client.getTaxRegistrationNumber())) {
            lines.add("Matricule fiscal: " + client.getTaxRegistrationNumber());
        }
        if (hasText(client.getAddress()) && !"N/A".equalsIgnoreCase(client.getAddress().trim())) {
            lines.add(client.getAddress());
        }
        if (hasText(client.getPhone())) {
            lines.add(client.getPhone());
        }
        return lines;
    }

    private Image loadLogo(BillGenerationRequest request) {
        if (!hasText(request.getLogoBase64())) {
            return null;
        }
        try {
            String payload = request.getLogoBase64().trim();
            if (payload.contains(",")) {
                payload = payload.substring(payload.indexOf(',') + 1);
            }
            byte[] bytes = Base64.getDecoder().decode(payload);
            return Image.getInstance(bytes);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void addHeaderCell(PdfPTable table, String label) {
        PdfPCell cell = borderedCell(label, BOLD_FONT, Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String value, int alignment) {
        table.addCell(borderedCell(value, NORMAL_FONT, alignment));
    }

    private PdfPCell borderedCell(String value, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(valueOrDefault(value, ""), font));
        cell.setPadding(5f);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private PdfPCell noBorderCell(String value, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(valueOrDefault(value, ""), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(0f);
        cell.setPaddingBottom(2f);
        return cell;
    }

    private PdfPCell emptyCell() {
        PdfPCell cell = new PdfPCell(new Phrase(""));
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(3, RoundingMode.HALF_UP);
    }

    private BigDecimal percent(BigDecimal value) {
        return money(value).divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
    }

    private String formatMoney(BigDecimal value, String currency) {
        BigDecimal amount = money(value);
        int decimals = isEuro(currency) ? 2 : 3;
        String plain = amount.setScale(decimals, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
        String[] parts = plain.split(",");
        String intPart = parts[0].replaceAll("\\B(?=(\\d{3})+(?!\\d))", " ");
        if (parts.length == 1) {
            return intPart;
        }
        String frac = parts[1];
        if (decimals == 3 && frac.equals("000")) {
            return intPart;
        }
        return intPart + "," + frac;
    }

    private String formatQty(BigDecimal value) {
        BigDecimal qty = value == null ? BigDecimal.ZERO : value.stripTrailingZeros();
        if (qty.scale() <= 0) {
            return qty.toPlainString();
        }
        return qty.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }

    private String formatQtyWithUnit(BigDecimal quantity, String unit) {
        String formatted = formatQty(quantity);
        if (unit == null || unit.isBlank() || "SERVICE".equals(unit) || "UNIT".equals(unit)) {
            return formatted;
        }
        return formatted + " " + unit;
    }

    private String resolvePriceUnitSuffix(List<BillLineDto> lines) {
        if (lines == null || lines.isEmpty()) {
            return "";
        }
        String unit = lines.get(0).getUnit();
        if (unit == null || unit.isBlank() || "SERVICE".equals(unit) || "UNIT".equals(unit)) {
            return "";
        }
        return "/" + unit;
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return "TND";
        }
        String normalized = currency.trim().toUpperCase(Locale.ROOT);
        return "EUR".equals(normalized) ? "EURO" : normalized;
    }

    private boolean isEuro(String currency) {
        return "EURO".equals(currency) || "EUR".equals(currency);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private String safeFileName(String value) {
        return value == null ? "draft" : value.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
