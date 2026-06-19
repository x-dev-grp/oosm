package com.xdev.ooms.documents.form.mapper;

import com.xdev.ooms.production.parameter.entity.Parameter;
import com.xdev.ooms.production.parameter.service.ParameterService;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;
import com.xdev.ooms.documents.form.FormPdfLabels;
import com.xdev.ooms.documents.form.dto.FormPdfConfigDto;
import com.xdev.ooms.documents.form.dto.FormPdfFieldDto;
import com.xdev.ooms.documents.form.dto.FormPdfFooterDto;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Component
public class ProductionPdfConfigMapper {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String PRIX_TRITURATION_KG = "PRIX_TRITURATION_KG";
    private static final double DEFAULT_MILLING_PRICE = 0.170d;

    private final ParameterService parameterService;

    public ProductionPdfConfigMapper(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public FormPdfConfigDto map(UnifiedDelivery delivery) {
        double prixUnitaire = resolveMillingPrice();

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

    private double resolveMillingPrice() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            return DEFAULT_MILLING_PRICE;
        }
        try {
            Parameter parameter = parameterService.getByCode(PRIX_TRITURATION_KG, tenantId);
            if (parameter != null && parameter.getValue() != null && !parameter.getValue().isBlank()) {
                double parsed = Double.parseDouble(parameter.getValue().replace(',', '.'));
                return parsed > 0 ? parsed : DEFAULT_MILLING_PRICE;
            }
        } catch (Exception ignored) {
            // fall back to frontend default
        }
        return DEFAULT_MILLING_PRICE;
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
