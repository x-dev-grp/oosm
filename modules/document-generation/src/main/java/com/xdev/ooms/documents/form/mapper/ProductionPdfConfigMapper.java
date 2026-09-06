package com.xdev.ooms.documents.form.mapper;

import com.xdev.ooms.production.parameter.service.SeasonPricingParameterReader;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;
import com.xdev.ooms.documents.form.FormPdfLabels;
import com.xdev.ooms.documents.form.dto.FormPdfConfigDto;
import com.xdev.ooms.documents.form.dto.FormPdfFieldDto;
import com.xdev.ooms.documents.form.dto.FormPdfFooterDto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class ProductionPdfConfigMapper {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final SeasonPricingParameterReader seasonPricingParameterReader;

    public ProductionPdfConfigMapper(SeasonPricingParameterReader seasonPricingParameterReader) {
        this.seasonPricingParameterReader = seasonPricingParameterReader;
    }

    public FormPdfConfigDto map(UnifiedDelivery delivery) {
        LocalDate pricingDate = delivery.getTrtDate() != null
                ? delivery.getTrtDate().toLocalDate()
                : (delivery.getDeliveryDate() != null ? delivery.getDeliveryDate().toLocalDate() : LocalDate.now());
        String varietyName = nameOf(delivery.getOliveVariety(), null);
        double prixUnitaire = seasonPricingParameterReader.resolvePricePerKg(pricingDate, varietyName);

        double qteHuile = delivery.getOilQuantity() == null ? 0d : delivery.getOilQuantity();
        double qteOlive = delivery.getPoidsNet() == null ? 0d : delivery.getPoidsNet();
        double rendement = delivery.getRendement() == null ? 0d : delivery.getRendement();
        String storageUnit = delivery.getStorageUnit() == null ? "N/A" : safe(delivery.getStorageUnit().getName());
        String dateTrituration = delivery.getTrtDate() == null
                ? LocalDateTime.now().format(DATE_FMT)
                : delivery.getTrtDate().format(DATE_FMT);

        FormPdfConfigDto config = new FormPdfConfigDto();
        config.setTitle(FormPdfLabels.PRODUCTION_RECEIPT);
        config.setReference("FOR-CQH-01");
        config.setRevision("00");
        config.setDate("01/12/2024");
        config.setNumber(deliveryNumberSuffix(delivery));
        config.setQrPayload(firstNonBlank(
                delivery.getQrHex(),
                delivery.getLotNumber(),
                delivery.getDeliveryNumber(),
                delivery.getId() != null ? delivery.getId().toString() : null));

        List<FormPdfFieldDto> generalInfo = new ArrayList<>();
        generalInfo.add(field(FormPdfLabels.LOT_NUMBER,
                firstNonBlank(delivery.getGlobalLotNumber(), delivery.getLotNumber(), "-")));
        generalInfo.add(field(FormPdfLabels.RECEPTION_NUMBER, safeOr(delivery.getDeliveryNumber(), "-")));
        generalInfo.add(field(FormPdfLabels.SUPPLIER, supplierName(delivery)));
        generalInfo.add(field(FormPdfLabels.REGION, nameOf(delivery.getRegion(), "-")));
        generalInfo.add(field(FormPdfLabels.OLIVE_VARIETY, nameOf(delivery.getOliveVariety(), "-")));
        generalInfo.add(field(FormPdfLabels.OLIVE_TYPE,
                delivery.getOliveType() == null ? "-" : delivery.getOliveType().name()));
        config.setGeneralInfo(generalInfo);

        List<FormPdfFieldDto> fields = new ArrayList<>();
        fields.add(field(FormPdfLabels.CRUSHING_DATE, dateTrituration));
        fields.add(field(FormPdfLabels.OLIVE_QUANTITY, String.format(Locale.FRENCH, "%s kg", trimNumber(qteOlive))));
        fields.add(field(FormPdfLabels.OIL_QUANTITY, String.format(Locale.FRENCH, "%s kg", trimNumber(qteHuile))));
        fields.add(field(FormPdfLabels.YIELD, String.format(Locale.FRENCH, "%.3f %%", rendement)));
        fields.add(field(FormPdfLabels.STORAGE_UNIT, storageUnit + " "));
        fields.add(field(FormPdfLabels.MILLING_PRICE,
                String.format(Locale.FRENCH, "%s TND/kg", trimNumber(prixUnitaire))));
        config.setFields(fields);

        config.setFooterInfo(List.of(
                new FormPdfFooterDto(FormPdfLabels.QUALITY_MANAGER),
                new FormPdfFooterDto(FormPdfLabels.PRODUCTION_MANAGER),
                new FormPdfFooterDto(FormPdfLabels.SIGNATURE),
                new FormPdfFooterDto(FormPdfLabels.DATE)));

        String lot = safe(delivery.getLotNumber());
        config.setFileName("BonProduction_" + (lot.isBlank() ? "LOT" : lot));
        return config;
    }

    private FormPdfFieldDto field(String label, String value) {
        return new FormPdfFieldDto(label, value);
    }

    private String deliveryNumberSuffix(UnifiedDelivery delivery) {
        String year = String.valueOf(LocalDateTime.now().getYear()).substring(2);
        return safe(delivery.getDeliveryNumber()) + " / " + year;
    }

    private String supplierName(UnifiedDelivery delivery) {
        if (delivery.getSupplier() == null) {
            return "-";
        }
        String name = (safe(delivery.getSupplier().getName()) + " " + safe(delivery.getSupplier().getLastname())).trim();
        return name.isBlank() ? "-" : name;
    }

    private String nameOf(com.xdev.ooms.sharedkernel.basetype.entity.BaseType baseType, String fallback) {
        if (baseType == null || baseType.getName() == null || baseType.getName().isBlank()) {
            return fallback;
        }
        return baseType.getName().trim();
    }

    private String trimNumber(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "-";
    }

    private String safeOr(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
