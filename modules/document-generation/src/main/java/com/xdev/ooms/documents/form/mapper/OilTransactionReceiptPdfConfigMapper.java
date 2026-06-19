package com.xdev.ooms.documents.form.mapper;

import com.xdev.ooms.documents.form.FormPdfLabels;
import com.xdev.ooms.documents.form.dto.FormPdfConfigDto;
import com.xdev.ooms.documents.form.dto.FormPdfFieldDto;
import com.xdev.ooms.documents.form.dto.FormPdfFooterDto;
import com.xdev.ooms.production.oiltransaction.entity.OilTransaction;
import com.xdev.ooms.production.supplier.entity.Supplier;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;
import com.xdev.ooms.sharedkernel.Enum.TransactionState;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class OilTransactionReceiptPdfConfigMapper {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH);

    public FormPdfConfigDto map(OilTransaction transaction) {
        UnifiedDelivery reception = transaction.getReception();
        String reference = firstNonBlank(
                reception == null ? null : reception.getLotNumber(),
                transaction.getId() == null ? null : transaction.getId().toString());

        FormPdfConfigDto config = new FormPdfConfigDto();
        config.setTitle(FormPdfLabels.TRANSACTION_OIL);
        config.setReference(reference);
        config.setDate(formatDate(transaction.getCreatedDate()));

        List<FormPdfFieldDto> generalInfo = new ArrayList<>();
        generalInfo.add(field(FormPdfLabels.DATE, formatDate(transaction.getCreatedDate())));
        generalInfo.add(field(FormPdfLabels.SOURCE_UNIT, storageUnitName(transaction.getStorageUnitSource())));
        generalInfo.add(field(FormPdfLabels.DESTINATION_UNIT, storageUnitName(transaction.getStorageUnitDestination())));
        generalInfo.add(field(FormPdfLabels.TRANSACTION_STATE, transactionStateLabel(transaction.getTransactionState())));
        generalInfo.add(field(FormPdfLabels.CREATED_DATE, formatDate(transaction.getCreatedDate())));
        generalInfo.add(field(FormPdfLabels.QUALITY_GRADE, safe(transaction.getQualityGrade())));
        generalInfo.add(field(FormPdfLabels.RECEPTION_DATE, formatDate(transaction.getCreatedDate())));
        generalInfo.add(field(FormPdfLabels.QUANTITY,
                String.format(Locale.FRENCH, "%.0f kg", transaction.getQuantityKg() == null ? 0d : transaction.getQuantityKg())));
        config.setGeneralInfo(generalInfo);

        List<FormPdfFieldDto> fields = new ArrayList<>();
        fields.add(field(FormPdfLabels.RECEPTION_ID, reception == null ? "—" : safe(reception.getLotNumber())));
        fields.add(field(FormPdfLabels.RECEPTION_SUPPLIER, supplierName(reception == null ? null : reception.getSupplier())));
        fields.add(field(FormPdfLabels.RECEPTION_DATE, formatDate(reception == null ? null : reception.getDeliveryDate())));
        fields.add(field(FormPdfLabels.RECEPTION_REGION,
                reception == null || reception.getRegion() == null ? "—" : safe(reception.getRegion().getName())));
        config.setFields(fields);

        config.setFooterInfo(List.of(
                new FormPdfFooterDto(FormPdfLabels.SIGNATURE_AGENT),
                new FormPdfFooterDto(FormPdfLabels.SIGNATURE_RESPONSIBLE)));

        String fileRef = reception == null ? null : reception.getLotNumber();
        if (fileRef == null || fileRef.isBlank()) {
            fileRef = transaction.getId() == null ? "inconnu" : transaction.getId().toString();
        }
        config.setFileName("Bon_Transaction_Huile_" + fileRef + ".pdf");
        return config;
    }

    private FormPdfFieldDto field(String label, String value) {
        return new FormPdfFieldDto(label, value == null || value.isBlank() ? "—" : value);
    }

    private String storageUnitName(com.xdev.ooms.production.storageunit.entity.StorageUnit unit) {
        return unit == null ? "—" : safe(unit.getName());
    }

    private String supplierName(Supplier supplier) {
        if (supplier == null) {
            return "—";
        }
        String name = (safe(supplier.getName()) + " " + safe(supplier.getLastname())).trim();
        return name.isBlank() ? "—" : name;
    }

    private String transactionStateLabel(TransactionState state) {
        if (state == null) {
            return "—";
        }
        return switch (state) {
            case PENDING -> FormPdfLabels.TRANSACTION_STATE_PENDING;
            case COMPLETED -> FormPdfLabels.TRANSACTION_STATE_COMPLETED;
            case CANCELED -> FormPdfLabels.TRANSACTION_STATE_CANCELED;
        };
    }

    private String formatDate(LocalDateTime date) {
        return date == null ? "N/A" : date.format(DATE_FMT);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "—";
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "—" : value.trim();
    }
}
