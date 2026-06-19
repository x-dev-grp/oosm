package com.xdev.ooms.documents.expedition;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.xdev.ooms.documents.form.dto.FormPdfDocument;
import com.xdev.ooms.documents.layout.PdfLogoPlacement;
import com.xdev.ooms.sharedkernel.ports.CompanyProfileReadPort;
import com.xdev.ooms.sharedkernel.ports.CompanyProfileSnapshot;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class ExpeditionPdfGeneratorService {

    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.FRENCH);
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH);

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font SMALL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font MUTED_FONT = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, BaseColor.GRAY);
    private static final Font TABLE_HEAD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);

    private final CompanyProfileReadPort companyProfileReadPort;
    private final ObjectMapper objectMapper;

    public ExpeditionPdfGeneratorService(CompanyProfileReadPort companyProfileReadPort, ObjectMapper objectMapper) {
        this.companyProfileReadPort = companyProfileReadPort;
        this.objectMapper = objectMapper;
    }

    public FormPdfDocument generate(ExpeditionPdfConfig config) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 42, 42, 42, 42);
            PdfWriter.getInstance(document, out);
            document.open();

            drawHeader(document, config);
            drawDestinationAndLogistics(document, config);
            drawArticlesTable(document, config);
            drawTraceability(document, config);
            drawFooter(document, config);

            document.close();
            String fileName = "Expedition_" + safe(config.getReference(), "document") + ".pdf";
            return new FormPdfDocument(fileName, out.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate expedition PDF", e);
        }
    }

    private void drawHeader(Document document, ExpeditionPdfConfig config) throws DocumentException {
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{1.2f, 2f});

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(PdfPCell.NO_BORDER);
        logoCell.setFixedHeight(60f);
        Image logo = loadLogo();
        if (logo != null) {
            logo.scaleToFit(100, 60);
            logoCell.addElement(logo);
        }
        header.addCell(logoCell);

        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(PdfPCell.NO_BORDER);
        titleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        titleCell.addElement(new Phrase(safe(config.getTitle(), ExpeditionPdfLabels.EXPEDITION_DELIVERY_TITLE).toUpperCase(Locale.ROOT), TITLE_FONT));
        titleCell.addElement(new Phrase(ExpeditionPdfLabels.REFERENCE + ": " + safe(config.getReference(), "—"), NORMAL_FONT));
        titleCell.addElement(new Phrase(ExpeditionPdfLabels.DATE + ": " + safe(config.getDate(), ""), NORMAL_FONT));
        header.addCell(titleCell);

        document.add(header);
        document.add(new Phrase(" "));
    }

    private void drawDestinationAndLogistics(Document document, ExpeditionPdfConfig config) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(8f);

        table.addCell(sectionCell(ExpeditionPdfLabels.CLIENT_DESTINATION));
        table.addCell(sectionCell(ExpeditionPdfLabels.LOGISTICS_INFO));

        PdfPCell left = new PdfPCell();
        left.setBorder(PdfPCell.NO_BORDER);
        addLine(left, safe(config.getClientName(), ""));
        addLine(left, firstNonBlank(config.getDestination(), config.getClientAddress()));
        if (hasText(config.getClientPhone())) {
            addLine(left, ExpeditionPdfLabels.PHONE + ": " + config.getClientPhone());
        }
        table.addCell(left);

        PdfPCell right = new PdfPCell();
        right.setBorder(PdfPCell.NO_BORDER);
        if (hasText(config.getCarrier())) {
            addLine(right, ExpeditionPdfLabels.CARRIER + ": " + config.getCarrier());
        }
        if (hasText(config.getDriver())) {
            addLine(right, ExpeditionPdfLabels.DRIVER + ": " + config.getDriver());
        }
        if (hasText(config.getTruck())) {
            addLine(right, ExpeditionPdfLabels.TRUCK + ": " + config.getTruck());
        }
        if (hasText(config.getIncoterm())) {
            addLine(right, ExpeditionPdfLabels.INCOTERM + ": " + config.getIncoterm());
        }
        table.addCell(right);

        document.add(table);
        document.add(new Phrase(" "));
    }

    private void drawArticlesTable(Document document, ExpeditionPdfConfig config) throws DocumentException {
        document.add(new Phrase(ExpeditionPdfLabels.SHIPMENT_CONTENT, SECTION_FONT));
        document.add(new Phrase(" "));

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.2f, 2f, 1.2f, 1.6f});
        addHeadCell(table, ExpeditionPdfLabels.OF);
        addHeadCell(table, ExpeditionPdfLabels.ARTICLE);
        addHeadCell(table, ExpeditionPdfLabels.QUANTITY);
        addHeadCell(table, ExpeditionPdfLabels.LOT_NUMBER);

        for (ExpeditionPdfConfig.Line line : config.getLines()) {
            addBodyCell(table, safe(line.getOfCode(), "—"));
            addBodyCell(table, safe(line.getArticleName(), "—"));
            String qty = line.getQuantity() == null ? "0" : String.valueOf(line.getQuantity());
            addBodyCell(table, qty + " " + safe(line.getUnit(), ""));
            addBodyCell(table, safe(line.getLotNumber(), "—"));
        }

        document.add(table);
        document.add(new Phrase(" "));
    }

    private void drawTraceability(Document document, ExpeditionPdfConfig config) throws DocumentException {
        Map<String, Object> traceability = config.getTraceability();
        if (traceability == null || traceability.isEmpty()) {
            return;
        }

        document.add(new Phrase(ExpeditionPdfLabels.TRACEABILITY_GENEALOGY, SECTION_FONT));
        document.add(new Phrase(" "));

        List<Map<String, Object>> eventChains = readListOfMaps(traceability.get("eventChains"));
        if (!eventChains.isEmpty()) {
            for (Map<String, Object> chain : eventChains) {
                document.add(new Phrase("OF: " + safe(asString(chain.get("ofCode")), "—"), NORMAL_FONT));
                List<Map<String, Object>> events = readListOfMaps(chain.get("events"));
                events.sort(Comparator.comparingInt(e -> asInt(e.get("sequence"), 0)));
                for (Map<String, Object> event : events) {
                    String phase = safe(asString(event.get("phase")), "");
                    String type = safe(asString(event.get("type")), "");
                    String title = safe(asString(event.get("title")), "—");
                    String timestamp = formatTimestamp(asString(event.get("timestamp")));
                    document.add(new Phrase("  " + phase + " · " + type, SMALL_FONT));
                    document.add(new Phrase("  " + title, NORMAL_FONT));
                    if (hasText(timestamp)) {
                        document.add(new Phrase("  " + timestamp, SMALL_FONT));
                    }
                }
                document.add(new Phrase(" "));
            }
        } else {
            Map<String, Object> genealogy = readMap(traceability.get("oilGenealogy"));
            Map<String, Object> ofDetails = readMap(traceability.get("ofDetails"));
            for (Map.Entry<String, Object> entry : genealogy.entrySet()) {
                Map<String, Object> ofInfo = findOfInfo(ofDetails, entry.getKey());
                String lotLabel = firstNonBlank(
                        asString(ofInfo.get("traceabilityLotId")),
                        asString(ofInfo.get("lotVracId")),
                        entry.getKey());
                document.add(new Phrase(
                        "OF: " + safe(asString(ofInfo.get("code")), "—") + " | Lot: " + lotLabel,
                        NORMAL_FONT));
                appendGenealogyLines(document, readMap(entry.getValue()));
                document.add(new Phrase(" "));
            }
        }

        String capturedAt = formatTimestamp(asString(traceability.get("capturedAt")));
        if (hasText(capturedAt)) {
            document.add(new Phrase(
                    ExpeditionPdfLabels.SNAPSHOT_CAPTURED_AT + ": " + capturedAt,
                    MUTED_FONT));
        }
    }

    private void appendGenealogyLines(Document document, Map<String, Object> genea) throws DocumentException {
        if (genea.isEmpty()) {
            return;
        }
        List<Map<String, Object>> roots = readListOfMaps(genea.get("rootSources"));
        if (roots.isEmpty() && genea.get("rootSource") instanceof Map<?, ?> rootSource) {
            roots = List.of(readMap(rootSource));
        }
        int index = 1;
        for (Map<String, Object> root : roots) {
            String origin = roots.size() > 1 ? "ORIGINE " + index : "ORIGINE";
            document.add(new Phrase("  " + origin + " (" + safe(asString(root.get("type")), "") + ")", SMALL_FONT));
            document.add(new Phrase("  " + safe(asString(root.get("supplierName")), "—"), NORMAL_FONT));
            document.add(new Phrase(
                    "  Lot: " + safe(asString(root.get("lotNumber")), "—")
                            + " | Date: " + formatDateOnly(asString(root.get("date"))),
                    SMALL_FONT));
            index++;
        }

        for (Map<String, Object> filt : readListOfMaps(genea.get("filtrations"))) {
            document.add(new Phrase("  FILTRATION", SMALL_FONT));
            document.add(new Phrase("  Lot Cible: " + safe(asString(filt.get("targetLotNumber")), "—"), NORMAL_FONT));
            document.add(new Phrase(
                    "  Volume: " + safe(asString(filt.get("volumeFiltered")), "—")
                            + " L | " + formatTimestamp(asString(filt.get("timestamp"))),
                    SMALL_FONT));
        }

        document.add(new Phrase("  UNITÉ DE STOCKAGE FINALE", SMALL_FONT));
        document.add(new Phrase("  " + safe(asString(genea.get("storageUnitName")), "—"), NORMAL_FONT));
        document.add(new Phrase("  Lot Final: " + safe(asString(genea.get("lotNumber")), "—"), SMALL_FONT));
    }

    private void drawFooter(Document document, ExpeditionPdfConfig config) throws DocumentException {
        String footer = firstNonBlank(config.getCompanyAddress(), companyAddress());
        if (!hasText(footer)) {
            return;
        }
        document.add(new Phrase(" "));
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(footer, MUTED_FONT));
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        document.add(table);
    }

    private PdfPCell sectionCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, SECTION_FONT));
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    private void addLine(PdfPCell cell, String text) {
        if (hasText(text)) {
            cell.addElement(new Phrase(text, NORMAL_FONT));
        }
    }

    private void addHeadCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TABLE_HEAD_FONT));
        cell.setBackgroundColor(new BaseColor(40, 40, 40));
        cell.setPadding(6f);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, SMALL_FONT));
        cell.setPadding(5f);
        table.addCell(cell);
    }

    private Image loadLogo() {
        Optional<CompanyProfileSnapshot> profile = companyProfileReadPort.findCurrentTenantProfile();
        if (profile.isEmpty() || !hasText(profile.get().logoBase64())) {
            return null;
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(profile.get().logoBase64().trim());
            return Image.getInstance(bytes);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String companyAddress() {
        return companyProfileReadPort.findCurrentTenantProfile()
                .map(CompanyProfileSnapshot::address)
                .orElse("");
    }

    private Map<String, Object> findOfInfo(Map<String, Object> ofDetails, String anchorKey) {
        if (ofDetails == null || ofDetails.isEmpty()) {
            return Map.of();
        }
        Object direct = ofDetails.get(anchorKey);
        if (direct instanceof Map<?, ?> map) {
            return readMap(map);
        }
        for (Object value : ofDetails.values()) {
            if (value instanceof Map<?, ?> map) {
                Map<String, Object> parsed = readMap(map);
                if (anchorKey.equals(asString(parsed.get("traceabilityLotId")))
                        || anchorKey.equals(asString(parsed.get("lotVracId")))) {
                    return parsed;
                }
            }
        }
        return Map.of();
    }

    private List<Map<String, Object>> readListOfMaps(Object value) {
        if (value instanceof List<?> list) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    result.add(readMap(map));
                }
            }
            return result;
        }
        return List.of();
    }

    private Map<String, Object> readMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                result.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            return result;
        }
        return Map.of();
    }

    private String formatTimestamp(String value) {
        if (!hasText(value)) {
            return "";
        }
        try {
            return LocalDateTime.parse(value).format(DATE_TIME_FMT);
        } catch (Exception ignored) {
            return value;
        }
    }

    private String formatDateOnly(String value) {
        if (!hasText(value)) {
            return "—";
        }
        try {
            return LocalDateTime.parse(value).format(DATE_FMT);
        } catch (Exception ignored) {
            return value;
        }
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private int asInt(Object value, int fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return value == null ? fallback : Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private String safe(String value, String fallback) {
        return hasText(value) ? value.trim() : fallback;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
