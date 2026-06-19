package com.xdev.ooms.documents.form.mapper;

import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;
import com.xdev.ooms.documents.form.FormPdfLabels;
import com.xdev.ooms.documents.form.OilGradeExtractor;
import com.xdev.ooms.documents.form.dto.FormPdfConfigDto;
import com.xdev.ooms.documents.form.dto.FormPdfFieldDto;
import com.xdev.ooms.documents.form.dto.FormPdfFooterDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class OilReceptionPdfConfigMapper {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public FormPdfConfigDto map(UnifiedDelivery delivery) {
        FormPdfConfigDto config = new FormPdfConfigDto();
        config.setTitle(FormPdfLabels.RECEPTION_OIL);
        config.setReference("FOR-ACH-21");
        config.setRevision("00");
        config.setDate("01/12/2024");
        config.setNumber(deliveryNumberSuffix(delivery));

        List<FormPdfFieldDto> generalInfo = new ArrayList<>();
        generalInfo.add(field(FormPdfLabels.DATE, formatDate(delivery.getDeliveryDate())));
        generalInfo.add(field(FormPdfLabels.SUPPLIER, supplierName(delivery)));
        generalInfo.add(field(FormPdfLabels.RECPT_NOTE_OR_OLIVE_LOT_NUMBER,
                firstNonBlank(delivery.getLotOliveNumber(), delivery.getDeliveryNumber())));
        generalInfo.add(field(FormPdfLabels.CODEECH, safe(delivery.getLotNumber())));
        generalInfo.add(field(FormPdfLabels.QUALITY,
                OilGradeExtractor.gradeLabel(OilGradeExtractor.extractGrade(delivery))));
        generalInfo.add(field(FormPdfLabels.LIEU_DU_STOCKAGE,
                delivery.getStorageUnit() == null ? "N/A" : safe(delivery.getStorageUnit().getName())));
        generalInfo.add(field(FormPdfLabels.OIL_QUANTITY,
                String.format(Locale.FRENCH, "%.2f kg", delivery.getOilQuantity() == null ? 0d : delivery.getOilQuantity())));
        generalInfo.add(field(FormPdfLabels.UNIT_PRICE,
                String.format(Locale.FRENCH, "%.2f TND/kg", delivery.getUnitPrice() == null ? 0d : delivery.getUnitPrice())));
        config.setGeneralInfo(generalInfo);
        config.setFields(List.of());

        config.setFooterInfo(List.of(
                new FormPdfFooterDto(FormPdfLabels.SIGNATURE_AGENT),
                new FormPdfFooterDto(FormPdfLabels.SIGNATURE_RESPONSIBLE)));

        String deliveryNumber = safe(delivery.getDeliveryNumber());
        config.setFileName("Bon_Reception_Huile_" + (deliveryNumber.isBlank() ? "inconnu" : deliveryNumber) + ".pdf");
        return config;
    }

    private FormPdfFieldDto field(String label, String value) {
        return new FormPdfFieldDto(label, value == null || value.isBlank() ? "—" : value);
    }

    private String deliveryNumberSuffix(UnifiedDelivery delivery) {
        String year = String.valueOf(LocalDateTime.now().getYear()).substring(2);
        return safe(delivery.getDeliveryNumber()) + " / " + year;
    }

    private String formatDate(LocalDateTime date) {
        return date == null ? "" : date.format(DATE_FMT);
    }

    private String supplierName(UnifiedDelivery delivery) {
        if (delivery.getSupplier() == null) {
            return "";
        }
        String first = safe(delivery.getSupplier().getName());
        String last = safe(delivery.getSupplier().getLastname());
        return (first + " " + last).trim();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
