package com.xdev.ooms.production.oilsale.entity;


import com.xdev.ooms.production.supplier.entity.Supplier;
import com.xdev.ooms.sharedkernel.Enum.*;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Oil Sale entity for managing oil sales transactions
 */
@Entity
public class OilSale extends BaseEntity implements Serializable {

    private Double paidAmount = 0d;
    private Double unpaidAmount = 0d;
    private boolean paid = false;

    @Column(unique = true, length = 50)
    private String invoiceNumber;
    @Enumerated(EnumType.STRING)
    private QualityGrades qualityGrade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SaleStatus status = SaleStatus.PENDING;
    @Column(nullable = false)
    private LocalDateTime saleDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;
    private UUID storageUnit;
    @Enumerated(EnumType.STRING)
    @Column(name = "oil_type")
    private Olive_Oil_Type oilType;
    @Column(precision = 10, scale = 2)
    private BigDecimal quantity;


    @Column(precision = 10, scale = 2)
    private BigDecimal unitPrice;


    @Column(precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO; // keep non-null for safety

    @Enumerated(EnumType.ORDINAL)
    private Currency currency;

    @Enumerated(EnumType.ORDINAL)
    private PaymentMethod paymentMethod;

    @Column(length = 100)
    private String bankAccount;

    @Column(length = 50)
    private String checkNumber;

    @Column(length = 100)
    private String externalTransactionId;

    @Column(length = 1000)
    private String description;


    private LocalDateTime deliveryDate;

    @Column(length = 500)
    private String deliveryAddress;

    @Column(length = 1000)
    private String deliveryNotes;

    public void setQualityGrade(QualityGrades qualityGrade) {
        this.qualityGrade = qualityGrade;
    }


    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = (paidAmount == null) ? 0d : paidAmount;
    }

    public void setUnpaidAmount(Double unpaidAmount) {
        this.unpaidAmount = (unpaidAmount == null) ? 0d : unpaidAmount;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
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

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public void setStorageUnit(UUID storageUnit) {
        this.storageUnit = storageUnit;
    }

    public void setOilType(Olive_Oil_Type oilType) {
        this.oilType = oilType;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        calculateTotalAmount();
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateTotalAmount();
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = (totalAmount == null) ? BigDecimal.ZERO : totalAmount;
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

    // ==================== BUSINESS LOGIC METHODS ====================

    /**
     * Calculate the total amount based on quantity and unit price
     */
    private void calculateTotalAmount() {
        if (quantity != null && unitPrice != null) {
            this.totalAmount = quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
        } else {
            this.totalAmount = BigDecimal.ZERO;
        }
    }

    /**
     * Check if the sale is confirmed
     */
    public boolean isConfirmed() {
        return SaleStatus.CONFIRMED.equals(status);
    }

    /**
     * Check if the sale is delivered
     */
    public boolean isDelivered() {
        return SaleStatus.DELIVERED.equals(status);
    }

    /**
     * Check if the sale is cancelled
     */
    public boolean isCancelled() {
        return SaleStatus.CANCELLED.equals(status);
    }

    /**
     * Check if the sale is pending
     */
    public boolean isPending() {
        return SaleStatus.PENDING.equals(status);
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public Double getUnpaidAmount() {
        return unpaidAmount;
    }

    public boolean isPaid() {
        return paid;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public QualityGrades getQualityGrade() {
        return qualityGrade;
    }

    public SaleStatus getStatus() {
        return status;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public UUID getStorageUnit() {
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
}
