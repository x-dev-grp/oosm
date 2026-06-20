package com.xdev.ooms.production.oilsale.dto;


import com.xdev.ooms.production.oilsale.entity.OilSale;
import com.xdev.ooms.production.storageunit.dto.StorageUnitDto;
import com.xdev.ooms.production.supplier.dto.SupplierDto;
import com.xdev.ooms.sharedkernel.Enum.*;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for OilSale entity
 */
public class OilSaleDTO extends BaseDto<OilSale> {
    private String invoiceNumber;
    private SaleStatus status;
    private LocalDateTime saleDate;
    private SupplierDto supplier;
    private StorageUnitDto storageUnit;
    private Olive_Oil_Type oilType;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private Currency currency;
    private PaymentMethod paymentMethod;
    private String bankAccount;
    private String checkNumber;
    private String externalTransactionId;
    private String description;
    private LocalDateTime deliveryDate;
    private String deliveryAddress;
    private String deliveryNotes;
    private boolean paid = false;
    private QualityGrades qualityGrade;
    private Double paidAmount;
    private Double unpaidAmount;
    private List<OilContainerSaleLineDto> containerSales = new ArrayList<>();

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public SaleStatus getStatus() {
        return status;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public SupplierDto getSupplier() {
        return supplier;
    }

    public StorageUnitDto getStorageUnit() {
        return storageUnit;
    }

    public Olive_Oil_Type getOilType() {
        return oilType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public String getDeliveryNotes() {
        return deliveryNotes;
    }

    public boolean isPaid() {
        return paid;
    }

    public QualityGrades getQualityGrade() {
        return qualityGrade;
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public Double getUnpaidAmount() {
        return unpaidAmount;
    }

    public List<OilContainerSaleLineDto> getContainerSales() {
        return containerSales;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public void setStatus(SaleStatus status) {
        this.status = status;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }

    public void setSupplier(SupplierDto supplier) {
        this.supplier = supplier;
    }

    public void setStorageUnit(StorageUnitDto storageUnit) {
        this.storageUnit = storageUnit;
    }

    public void setOilType(Olive_Oil_Type oilType) {
        this.oilType = oilType;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDeliveryDate(LocalDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public void setDeliveryNotes(String deliveryNotes) {
        this.deliveryNotes = deliveryNotes;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public void setQualityGrade(QualityGrades qualityGrade) {
        this.qualityGrade = qualityGrade;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public void setUnpaidAmount(Double unpaidAmount) {
        this.unpaidAmount = unpaidAmount;
    }

    public void setContainerSales(List<OilContainerSaleLineDto> containerSales) {
        this.containerSales = containerSales;
    }
}