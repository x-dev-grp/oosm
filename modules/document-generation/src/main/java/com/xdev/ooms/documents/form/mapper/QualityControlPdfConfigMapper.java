package com.xdev.ooms.documents.form.mapper;

import com.xdev.ooms.production.qualitycontrol.entity.QualityControlResult;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;
import com.xdev.ooms.documents.form.FormPdfLabels;
import com.xdev.ooms.documents.form.dto.FormPdfConfigDto;
import com.xdev.ooms.documents.form.dto.FormPdfFieldDto;
import com.xdev.ooms.documents.form.dto.FormPdfFooterDto;
import com.xdev.ooms.sharedkernel.Enum.DeliveryType;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class QualityControlPdfConfigMapper {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public FormPdfConfigDto map(UnifiedDelivery delivery) {
        DeliveryType type = delivery.getDeliveryType();
        boolean isOil = type == DeliveryType.OIL;
        boolean isOlive = type == DeliveryType.OLIVE;

        FormPdfConfigDto config = new FormPdfConfigDto();
        if (isOil) {
            config.setTitle(FormPdfLabels.FICH_CONTROL_QUALITE_HUIL);
        } else if (isOlive) {
            config.setTitle(FormPdfLabels.FICH_CONTROL_QUALITE_OLIVE);
        } else {
            config.setTitle(FormPdfLabels.FICH_CONTROL_QUALITE);
        }
        config.setReference("FOR-CQH-01");
        config.setRevision("0");
        config.setDate("01/12/2024");
        config.setNumber(deliveryNumberSuffix(delivery));

        List<FormPdfFieldDto> generalInfo = new ArrayList<>();
        generalInfo.add(field(FormPdfLabels.DATE, formatDate(delivery.getDeliveryDate(), "N/A")));
        generalInfo.add(field(FormPdfLabels.SUPPLIER, supplierName(delivery, "N/A")));
        generalInfo.add(field(FormPdfLabels.EXPENSE_RECEIPT_NUMBER, safeOr(delivery.getDeliveryNumber(), "N/A")));
        generalInfo.add(field(FormPdfLabels.VEHICLE_REGISTRATION, safeOr(delivery.getMatriculeCamion(), "N/A")));
        generalInfo.add(field(FormPdfLabels.TRUCK_STATE, safeOr(delivery.getEtatCamion(), "N/A")));
        config.setGeneralInfo(generalInfo);

        List<FormPdfFieldDto> fields = buildFields(delivery);
        config.setFields(fields);

        config.setFooterInfo(List.of(
                new FormPdfFooterDto(FormPdfLabels.SIGNATURE_AGENT),
                new FormPdfFooterDto(FormPdfLabels.SIGNATURE_RESPONSIBLE)));

        String suffix = isOil ? "Huile" : isOlive ? "Olive" : "Inconnu";
        String deliveryNumber = safe(delivery.getDeliveryNumber());
        config.setFileName("Bon_Reception_" + suffix + "_"
                + (deliveryNumber.isBlank() ? "inconnu" : deliveryNumber) + ".pdf");
        return config;
    }

    private List<FormPdfFieldDto> buildFields(UnifiedDelivery delivery) {
        List<FormPdfFieldDto> fields = new ArrayList<>();
        if (delivery.getQualityControlResults() == null || delivery.getQualityControlResults().isEmpty()) {
            fields.add(new FormPdfFieldDto(FormPdfLabels.NO_QUALITY_DATA, "N/A"));
            return fields;
        }

        Map<String, QualityControlResult> resultMap = new HashMap<>();
        for (QualityControlResult result : delivery.getQualityControlResults()) {
            if (result.getRule() == null) {
                continue;
            }
            String ruleName = result.getRule().getRuleName();
            if (ruleName != null) {
                resultMap.put(normalize(ruleName), result);
            }
        }

        for (QualityControlResult result : delivery.getQualityControlResults()) {
            if (result.getRule() == null) {
                continue;
            }
            String ruleName = safe(result.getRule().getRuleName());
            if (ruleName.isBlank()) {
                ruleName = "critère sans nom";
            }
            String label = pickLabel(resultMap, ruleName);
            String value = result.getMeasuredValue() == null ? "N/A" : String.valueOf(result.getMeasuredValue());
            fields.add(new FormPdfFieldDto(label, value));
        }
        return fields;
    }

    private String pickLabel(Map<String, QualityControlResult> resultMap, String ruleKey) {
        QualityControlResult result = resultMap.get(normalize(ruleKey));
        if (result != null && result.getRule() != null) {
            String description = result.getRule().getDescription();
            if (hasText(description)) {
                return description.trim();
            }
            String ruleName = result.getRule().getRuleName();
            if (hasText(ruleName)) {
                return ruleName.trim();
            }
        }
        return ruleKey;
    }

    private FormPdfFieldDto field(String label, String value) {
        return new FormPdfFieldDto(label, value);
    }

    private String deliveryNumberSuffix(UnifiedDelivery delivery) {
        String year = String.valueOf(LocalDateTime.now().getYear()).substring(2);
        return safe(delivery.getDeliveryNumber()) + " / " + year;
    }

    private String formatDate(LocalDateTime date, String fallback) {
        return date == null ? fallback : date.format(DATE_FMT);
    }

    private String supplierName(UnifiedDelivery delivery, String fallback) {
        if (delivery.getSupplier() == null) {
            return fallback;
        }
        String name = (safe(delivery.getSupplier().getName()) + " " + safe(delivery.getSupplier().getLastname())).trim();
        return name.isBlank() ? fallback : name;
    }

    private String normalize(String input) {
        if (input == null) {
            return "";
        }
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("\\s+", " ")
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String safeOr(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
