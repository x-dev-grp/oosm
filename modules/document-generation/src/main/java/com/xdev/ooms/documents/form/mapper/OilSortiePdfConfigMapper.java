package com.xdev.ooms.documents.form.mapper;

import com.xdev.ooms.documents.form.FormPdfLabels;
import com.xdev.ooms.documents.form.dto.FormPdfConfigDto;
import com.xdev.ooms.documents.form.dto.FormPdfFieldDto;
import com.xdev.ooms.documents.form.dto.FormPdfFooterDto;
import com.xdev.ooms.production.oiltransaction.entity.OilTransaction;
import com.xdev.ooms.production.supplier.entity.Supplier;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class OilSortiePdfConfigMapper {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH);

    public FormPdfConfigDto map(OilTransaction transaction) {
        UnifiedDelivery reception = transaction.getReception();
        String supplier = supplierName(reception == null ? null : reception.getSupplier());

        FormPdfConfigDto config = new FormPdfConfigDto();
        config.setTitle(FormPdfLabels.OIL_DISPATCH);
        config.setReference(transaction.getId() == null ? "—" : transaction.getId().toString());

        List<FormPdfFieldDto> generalInfo = new ArrayList<>();
        generalInfo.add(field(FormPdfLabels.DATE, formatDate(transaction.getCreatedDate())));
        generalInfo.add(field(FormPdfLabels.SUPPLIER, supplier));
        generalInfo.add(field(FormPdfLabels.LOT, reception == null ? "" : safe(reception.getLotNumber())));
        generalInfo.add(field(FormPdfLabels.OIL_TYPE, safe(transaction.getQualityGrade(), "N/A")));
        generalInfo.add(field(FormPdfLabels.RECIPIENT, supplier));
        config.setGeneralInfo(generalInfo);

        double quantity = transaction.getQuantityKg() == null ? 0d : transaction.getQuantityKg();
        double unitPrice = transaction.getUnitPrice() == null ? 0d : transaction.getUnitPrice();
        double totalPrice = transaction.getTotalPrice();

        List<FormPdfFieldDto> fields = new ArrayList<>();
        fields.add(field(FormPdfLabels.TANK_NUMBER, storageUnitName(transaction.getStorageUnitSource(), "N/A")));
        fields.add(field(FormPdfLabels.DESIGNATION, storageUnitName(transaction.getStorageUnitDestination(), "N/A")));
        fields.add(field(FormPdfLabels.QUANTITY, String.format(Locale.FRENCH, "%.2f kg", quantity)));
        fields.add(field(FormPdfLabels.UNIT_PRICE, String.format(Locale.FRENCH, "%.2f TND/kg", unitPrice)));
        fields.add(field(FormPdfLabels.TOTAL_PRICE, String.format(Locale.FRENCH, "%.2f TND", totalPrice)));
        config.setFields(fields);

        config.setFooterInfo(List.of(
                new FormPdfFooterDto(FormPdfLabels.SIGNATURE_AGENT),
                new FormPdfFooterDto(FormPdfLabels.SIGNATURE_RESPONSIBLE)));

        String fileRef = transaction.getId() == null ? "inconnu" : transaction.getId().toString();
        config.setFileName("Bon_Sortie_Huile_" + fileRef + ".pdf");
        return config;
    }

    private FormPdfFieldDto field(String label, String value) {
        return new FormPdfFieldDto(label, value == null || value.isBlank() ? "—" : value);
    }

    private String storageUnitName(com.xdev.ooms.production.storageunit.entity.StorageUnit unit, String fallback) {
        if (unit == null) {
            return fallback;
        }
        String name = unit.getName();
        return name == null || name.isBlank() ? fallback : name.trim();
    }

    private String supplierName(Supplier supplier) {
        if (supplier == null) {
            return "";
        }
        return (safe(supplier.getName()) + " " + safe(supplier.getLastname())).trim();
    }

    private String formatDate(LocalDateTime date) {
        return date == null ? "" : date.format(DATE_FMT);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String safe(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}
