package com.xdev.ooms.documents.form.mapper;

import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;
import com.xdev.ooms.documents.form.FormPdfLabels;
import com.xdev.ooms.documents.form.dto.FormPdfConfigDto;
import com.xdev.ooms.documents.form.dto.FormPdfFieldDto;
import com.xdev.ooms.documents.form.dto.FormPdfFooterDto;
import com.xdev.ooms.sharedkernel.Enum.DeliveryType;
import com.xdev.ooms.sharedkernel.Enum.Olive_Oil_Type;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class OliveReceptionPdfConfigMapper {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public FormPdfConfigDto map(UnifiedDelivery delivery) {
        FormPdfConfigDto config = new FormPdfConfigDto();
        config.setTitle(FormPdfLabels.RECEPTION_OLIVE);
        config.setReference("FOR-CQH-01");
        config.setRevision("00");
        config.setDate("01/12/2024");
        config.setNumber(deliveryNumberSuffix(delivery));

        List<FormPdfFieldDto> generalInfo = new ArrayList<>();
        generalInfo.add(field(FormPdfLabels.DATE, formatDate(delivery.getDeliveryDate())));
        generalInfo.add(field(FormPdfLabels.PRODUCER, supplierName(delivery)));
        generalInfo.add(field(FormPdfLabels.FERME, nameOf(delivery.getParcel())));
        generalInfo.add(field(FormPdfLabels.LOT, safe(delivery.getLotNumber())));
        generalInfo.add(field(FormPdfLabels.TYPE, oliveTypeLabel(delivery)));
        config.setGeneralInfo(generalInfo);

        List<FormPdfFieldDto> fields = new ArrayList<>();
        fields.add(field(FormPdfLabels.PARCEL, nameOf(delivery.getParcel())));
        fields.add(field(FormPdfLabels.OLIVE_VARIETY, nameOf(delivery.getOliveVariety())));
        fields.add(field(FormPdfLabels.NCOLIS, delivery.getSackCount() > 0 ? String.valueOf(delivery.getSackCount()) : "N/A"));
        fields.add(field(FormPdfLabels.GROSS_WEIGHT, weightKg(delivery.getPoidsBrute())));
        fields.add(field(FormPdfLabels.NET_WEIGHT, weightKg(delivery.getPoidsNet())));
        config.setFields(fields);

        config.setFooterInfo(List.of(
                new FormPdfFooterDto(FormPdfLabels.SIGNATURE_AGENT),
                new FormPdfFooterDto(FormPdfLabels.TRANSPORTER),
                new FormPdfFooterDto(FormPdfLabels.BASCULE)));

        String deliveryNumber = safe(delivery.getDeliveryNumber());
        config.setFileName("Bon_Reception_Olive_" + (deliveryNumber.isBlank() ? "inconnu" : deliveryNumber) + ".pdf");
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

    private String oliveTypeLabel(UnifiedDelivery delivery) {
        if (delivery.getDeliveryType() == DeliveryType.OLIVE && delivery.getOliveType() == Olive_Oil_Type.OC) {
            return "Conventionnelle";
        }
        if (delivery.getDeliveryType() == DeliveryType.OIL && delivery.getOilType() == Olive_Oil_Type.OC) {
            return "Conventionnelle";
        }
        return "Biologique";
    }

    private String nameOf(com.xdev.ooms.sharedkernel.basetype.entity.BaseType baseType) {
        return baseType == null ? "" : safe(baseType.getName());
    }

    private String weightKg(Double value) {
        return value == null ? "N/A" : String.format(Locale.FRENCH, "%s kg", trimTrailingZeros(value));
    }

    private String trimTrailingZeros(Double value) {
        if (value == null) {
            return "0";
        }
        if (value == Math.floor(value)) {
            return String.valueOf(value.longValue());
        }
        return String.valueOf(value);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
