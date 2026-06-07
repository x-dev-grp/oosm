package com.xdev.ooms.finance.model;

import com.xdev.ooms.production.model.Supplier;
import  com.xdev.ooms.sharedkernel.Enum.*;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Universal Financial Transaction Model
 * Handles all types of financial operations in the OSM system
 */
@Entity
public class FinancialTransaction extends BaseEntity {

    /**
     * PAYMENT, EXPENSE, CREDIT, DEBIT, TRANSFER, etc.
     */
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private OperationType operationType;

    @Enumerated(EnumType.STRING)
    private ResourceName resourceName;

    /**
     * INBOUND, OUTBOUND, INTERNAL
     */
    @Enumerated(EnumType.STRING)
    private TransactionDirection direction;

    /**
     * Primary amount in base currency
     */
    private BigDecimal amount = BigDecimal.ZERO;

    /**
     * Currency code (TND, USD, EUR, etc.)
     */
    @Enumerated(EnumType.STRING)
    private Currency currency;

    // ==================== AMOUNT & CURRENCY FIELDS ====================
    /**
     * CASH, CHEQUE, TRANSFER, etc.
     */
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    /**
     * Bank account used (if applicable)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id")
    private BankAccount bankAccount;

    // ==================== PAYMENT METHOD FIELDS ====================
    /**
     * Check number (if paymentMethod == CHEQUE)
     */
    @Column(name = "check_number", length = 50)
    private String checkNumber;

    /**
     * External system reference (e.g. for bank transfers)
     */
    private String externalTransactionId;

    // ==================== RELATIONSHIPS ====================
    private Double paidAmount = 0d;
    private Double unpaidAmount = 0d;

    /**
     * Related delivery lot number
     */
    @Column(name = "lot_number", length = 50)
    private String lotNumber;

    private String vendorName;

    /**
     * Supplier (if this is a supplier payment or credit)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    /**
     * Expense record (if type == EXPENSE)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id")
    private Expense expense;

    /**
     * When the transaction was made
     */
    private LocalDateTime transactionDate;

    /**
     * Has this been approved?
     */
    private Boolean approved = Boolean.FALSE;

    /**
     * When it was approved
     */
    private LocalDateTime approvalDate;

    /**
     * Who approved it
     */
    private String approvedBy;

    private String description;
    private String invoiceReference;
    private String receiptReference;

    // ==================== OPTIONAL METADATA ====================

    public ResourceName getResourceName() {
        return resourceName;
    }

    public void setResourceName(ResourceName resourceName) {
        this.resourceName = resourceName;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    // ==================== TIMING & APPROVAL ====================

    public Double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = (paidAmount == null) ? 0d : org.apache.commons.math3.util.Precision.round(paidAmount, 3);
    }

    public Double getUnpaidAmount() {
        return unpaidAmount;
    }

    public void setUnpaidAmount(Double unpaidAmount) {
        this.unpaidAmount = (unpaidAmount == null) ? 0d : org.apache.commons.math3.util.Precision.round(unpaidAmount, 3);
    }

    @Override
    public String toString() {
        return "FinancialTransaction{" +
                "transactionType=" + transactionType +
                ", direction=" + direction +
                ", amount=" + amount +
                ", currency=" + currency +
                ", paymentMethod=" + paymentMethod +
                ", bankAccount=" + bankAccount +
                ", checkNumber='" + checkNumber + '\'' +
                ", lotNumber='" + lotNumber + '\'' +
                ", supplier=" + supplier +
                ", expense=" + expense +
                ", description='" + description + '\'' +
                ", invoiceReference='" + invoiceReference + '\'' +
                ", receiptReference='" + receiptReference + '\'' +
                ", transactionDate=" + transactionDate +
                ", approved=" + approved +
                ", approvalDate=" + approvalDate +
                ", approvedBy='" + approvedBy + '\'' +
                '}';
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public TransactionDirection getDirection() {
        return direction;
    }

    public void setDirection(TransactionDirection direction) {
        this.direction = direction;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        // keep non-null and monetary scale 2
        this.amount = (amount == null) ? BigDecimal.ZERO : amount.setScale(2, RoundingMode.HALF_UP);
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public Expense getExpense() {
        return expense;
    }

    public void setExpense(Expense expense) {
        this.expense = expense;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInvoiceReference() {
        return invoiceReference;
    }

    public void setInvoiceReference(String invoiceReference) {
        this.invoiceReference = invoiceReference;
    }

    public String getReceiptReference() {
        return receiptReference;
    }

    public void setReceiptReference(String receiptReference) {
        this.receiptReference = receiptReference;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = (approved == null) ? Boolean.FALSE : approved;
    }

    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(LocalDateTime approvalDate) {
        this.approvalDate = approvalDate;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }
}
