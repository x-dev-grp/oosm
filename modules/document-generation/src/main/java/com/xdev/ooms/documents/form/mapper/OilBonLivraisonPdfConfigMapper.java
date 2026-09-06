package com.xdev.ooms.documents.form.mapper;

import com.xdev.ooms.documents.form.FormPdfLabels;
import com.xdev.ooms.documents.form.dto.FormPdfConfigDto;
import com.xdev.ooms.documents.form.dto.FormPdfFieldDto;
import com.xdev.ooms.documents.form.dto.FormPdfFooterDto;
import com.xdev.ooms.production.oilcontainersale.entity.OilContainerSale;
import com.xdev.ooms.production.oilcontainersale.repository.OilContainerSaleRepo;
import com.xdev.ooms.production.oilsale.entity.OilSale;
import com.xdev.ooms.production.storageunit.entity.StorageUnit;
import com.xdev.ooms.production.storageunit.repository.StorageUnitRepo;
import com.xdev.ooms.production.supplier.entity.Supplier;
import com.xdev.ooms.sharedkernel.Enum.QualityGrades;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class OilBonLivraisonPdfConfigMapper {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH);

    private final OilContainerSaleRepo containerSaleRepo;
    private final StorageUnitRepo storageUnitRepo;

    public OilBonLivraisonPdfConfigMapper(OilContainerSaleRepo containerSaleRepo, StorageUnitRepo storageUnitRepo) {
        this.containerSaleRepo = containerSaleRepo;
        this.storageUnitRepo = storageUnitRepo;
    }

    public FormPdfConfigDto map(OilSale sale) {
        BigDecimal quantity = sale.getQuantity() == null ? BigDecimal.ZERO : sale.getQuantity();
        BigDecimal unitPrice = sale.getUnitPrice() == null ? BigDecimal.ZERO : sale.getUnitPrice();
        BigDecimal oilLineTotal = quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
        BigDecimal saleTotal = sale.getTotalAmount() == null ? oilLineTotal : sale.getTotalAmount().setScale(2, RoundingMode.HALF_UP);
        List<OilContainerSale> containerLines = containerSaleRepo.findByOilSaleId(sale.getId());
        String storageUnitName = resolveStorageUnitName(sale);
        String documentDate = formatDate(sale.getDeliveryDate() != null ? sale.getDeliveryDate() : sale.getSaleDate());

        FormPdfConfigDto config = new FormPdfConfigDto();
        config.setTitle(FormPdfLabels.GEN_PDF_BON_LIVRAISON);
        config.setReference("FOR-LIV-01");
        config.setRevision("00");
        config.setDate(documentDate);

        List<FormPdfFieldDto> generalInfo = new ArrayList<>();
        generalInfo.add(field(FormPdfLabels.CLIENT, clientName(sale.getSupplier())));
        if (sale.getInvoiceNumber() != null && !sale.getInvoiceNumber().isBlank()) {
            generalInfo.add(field(FormPdfLabels.INVOICE_NUMBER, sale.getInvoiceNumber().trim()));
        }
        generalInfo.add(field(FormPdfLabels.STORAGE_UNIT, storageUnitName));
        generalInfo.add(field(FormPdfLabels.OIL_QUANTITY, qtyLiters(quantity)));
        generalInfo.add(field(FormPdfLabels.QUALITY, qualityLabel(sale.getQualityGrade())));
        generalInfo.add(field(FormPdfLabels.UNIT_PRICE, moneyPerLiter(unitPrice)));
        if (!containerLines.isEmpty()) {
            generalInfo.add(field(FormPdfLabels.CONTAINERS, containerSummary(containerLines)));
        }
        generalInfo.add(field(FormPdfLabels.TOTAL_PRICE, money(saleTotal)));
        generalInfo.add(field(FormPdfLabels.DELIVERY_DATE, formatDate(sale.getDeliveryDate())));
        generalInfo.add(field(FormPdfLabels.DELIVERY_ADDRESS, sale.getDeliveryAddress()));
        generalInfo.add(field(FormPdfLabels.DELIVERY_NOTES, sale.getDeliveryNotes()));
        generalInfo.add(field(FormPdfLabels.DATE, documentDate));
        config.setGeneralInfo(generalInfo);

        String designation = buildDesignation(sale.getDescription(), containerLines);
        List<FormPdfFieldDto> fields = new ArrayList<>();
        fields.add(field(FormPdfLabels.TANK_NUMBER, storageUnitName));
        fields.add(field(FormPdfLabels.DESIGNATION, designation));
        fields.add(field(FormPdfLabels.QUANTITY, qtyLiters(quantity)));
        fields.add(field(FormPdfLabels.UNIT_PRICE, moneyPerLiter(unitPrice)));
        fields.add(field(FormPdfLabels.TOTAL_PRICE, money(saleTotal)));
        config.setFields(fields);

        config.setFooterInfo(List.of(
                new FormPdfFooterDto(FormPdfLabels.SIGNATURE_AGENT),
                new FormPdfFooterDto(FormPdfLabels.SIGNATURE_RESPONSIBLE)));

        String fileRef = sale.getInvoiceNumber() != null && !sale.getInvoiceNumber().isBlank()
                ? sale.getInvoiceNumber().trim()
                : (sale.getId() == null ? "inconnu" : sale.getId().toString());
        config.setFileName("Bon_Livraison_Huile_" + fileRef + ".pdf");
        config.setQrPayload(firstNonBlank(
                sale.getQrHex(),
                sale.getInvoiceNumber(),
                sale.getId() != null ? sale.getId().toString() : null));
        return config;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private String resolveStorageUnitName(OilSale sale) {
        if (sale.getStorageUnit() == null) {
            return "—";
        }
        return storageUnitRepo.findByIdAndIsDeletedFalse(sale.getStorageUnit())
                .map(StorageUnit::getName)
                .filter(name -> name != null && !name.isBlank())
                .orElse(sale.getStorageUnit().toString());
    }

    private String buildDesignation(String description, List<OilContainerSale> containerLines) {
        String base = description == null || description.isBlank() ? "Vente huile" : description.trim();
        if (containerLines.isEmpty()) {
            return base;
        }
        return base + " + conteneurs";
    }

    private String containerSummary(List<OilContainerSale> containerLines) {
        return containerLines.stream()
                .map(line -> {
                    String name = line.getContainer() != null && line.getContainer().getName() != null
                            ? line.getContainer().getName()
                            : "Conteneur";
                    return line.getCount() + " x " + name;
                })
                .collect(Collectors.joining(", "));
    }

    private FormPdfFieldDto field(String label, String value) {
        return new FormPdfFieldDto(label, value == null || value.isBlank() ? "—" : value);
    }

    private String clientName(Supplier supplier) {
        if (supplier == null) {
            return "—";
        }
        String name = (safe(supplier.getName()) + " " + safe(supplier.getLastname())).trim();
        return name.isBlank() ? "—" : name;
    }

    private String qualityLabel(QualityGrades grade) {
        if (grade == null) {
            return "—";
        }
        return switch (grade) {
            case EXTRA_VIRGIN -> FormPdfLabels.OIL_GRADE_EXTRA_VIRGIN;
            case VIRGIN -> FormPdfLabels.OIL_GRADE_VIRGIN;
            case LAMPANTE -> FormPdfLabels.OIL_GRADE_LAMPANTE;
            case REFINED -> FormPdfLabels.OIL_GRADE_REFINED;
            case OTHER -> FormPdfLabels.OIL_GRADE_UNKNOWN;
            case POMACE -> FormPdfLabels.OIL_GRADE_POMACE;
        };
    }

    private String qtyLiters(BigDecimal quantity) {
        return String.format(Locale.FRENCH, "%.2f L", quantity.doubleValue());
    }

    private String money(BigDecimal amount) {
        return String.format(Locale.FRENCH, "%.2f TND", amount.doubleValue());
    }

    private String moneyPerLiter(BigDecimal amount) {
        return String.format(Locale.FRENCH, "%.2f TND/L", amount.doubleValue());
    }

    private String formatDate(LocalDateTime date) {
        return date == null ? "—" : date.format(DATE_FMT);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
