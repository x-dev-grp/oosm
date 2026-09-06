package com.xdev.ooms.production.dayimport.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DayImportWorkbook {
    private LocalDate businessDate;
    private String timezone;
    private final List<NamedRow> regions = new ArrayList<>();
    private final List<NamedRow> parcels = new ArrayList<>();
    private final List<NamedRow> supplierTypes = new ArrayList<>();
    private final List<SupplierRow> suppliers = new ArrayList<>();
    private final List<ContainerRow> containers = new ArrayList<>();
    private final List<QcRuleRow> qcRules = new ArrayList<>();
    private final List<ReceptionRow> receptions = new ArrayList<>();
    private final List<OilSaleRow> oilSales = new ArrayList<>();
    private final List<OilSaleContainerRow> oilSaleContainers = new ArrayList<>();
    private final List<ExpenseRow> expenses = new ArrayList<>();
    private final List<PaymentRow> payments = new ArrayList<>();
    private final List<QcResultRow> qcResults = new ArrayList<>();

    public LocalDate getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate(LocalDate businessDate) {
        this.businessDate = businessDate;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public List<NamedRow> getRegions() {
        return regions;
    }

    public List<NamedRow> getParcels() {
        return parcels;
    }

    public List<NamedRow> getSupplierTypes() {
        return supplierTypes;
    }

    public List<SupplierRow> getSuppliers() {
        return suppliers;
    }

    public List<ContainerRow> getContainers() {
        return containers;
    }

    public List<QcRuleRow> getQcRules() {
        return qcRules;
    }

    public List<ReceptionRow> getReceptions() {
        return receptions;
    }

    public List<OilSaleRow> getOilSales() {
        return oilSales;
    }

    public List<OilSaleContainerRow> getOilSaleContainers() {
        return oilSaleContainers;
    }

    public List<ExpenseRow> getExpenses() {
        return expenses;
    }

    public List<PaymentRow> getPayments() {
        return payments;
    }

    public List<QcResultRow> getQcResults() {
        return qcResults;
    }

    public static class NamedRow {
        public int rowNumber;
        public String name;
        public String description;
    }

    public static class SupplierRow {
        public int rowNumber;
        public String supplierKey;
        public String name;
        public String lastname;
        public String phone;
        public String matriculeFiscal;
        public String regionName;
        public String supplierTypeName;
    }

    public static class QcRuleRow {
        public int rowNumber;
        public String ruleKey;
        public String ruleName;
        public Boolean oilQc;
        public String ruleType;
        public Float minValue;
        public Float maxValue;
        public String ruleTextValue;
        public String description;
    }

    public static class ContainerRow {
        public int rowNumber;
        public String containerKey;
        public String name;
        public Double capacityInLiters;
        public Integer stockQuantity;
        public Double buyPrice;
        public Double sellingPrice;
    }

    public static class ReceptionRow {
        public int rowNumber;
        public String externalRef;
        public String deliveryType; // OLIVE | OIL
        /** OC|OB (HC|HB accepted). Used to build lot numbers like 0008OC26. */
        public String oliveOilType;
        /** Generic type name: OLIVE_VARIETY or OIL_VARIETY (e.g. Chemlali, Chetoui oil). */
        public String varietyName;
        public String operationType;
        public String supplierKey;
        public String regionName;
        public String parcelName;
        public Double poidsNet;
        public Double oilQuantity;
        public Double unitPrice;
        public String storageUnitKey;
        public String description;
    }

    public static class OilSaleRow {
        public int rowNumber;
        public String externalRef;
        public String invoiceNumber;
        public String supplierKey;
        public String storageUnitKey;
        public Double quantity;
        public Double unitPrice;
        public String currency;
        public String paymentMethod;
        public String qualityGrade;
        public Double paidAmount;
        public String description;
    }

    public static class OilSaleContainerRow {
        public int rowNumber;
        public String saleExternalRef;
        public String containerKey;
        public Integer count;
    }

    public static class ExpenseRow {
        public int rowNumber;
        public String externalRef;
        public Double amount;
        public String object;
        public String purchaseNature;
        public String category;
        public String paymentMethod;
        public String vendor;
        public String invoiceRef;
        public String notes;
    }

    public static class PaymentRow {
        public int rowNumber;
        public String receptionExternalRef;
        public Double amount;
        public String paymentMethod;
    }

    public static class QcResultRow {
        public int rowNumber;
        public String receptionExternalRef;
        public String ruleKey;
        public String value;
        public Boolean oilQc;
    }
}
