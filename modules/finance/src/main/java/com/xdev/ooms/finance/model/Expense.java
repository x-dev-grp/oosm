package com.xdev.ooms.finance.model;

import  com.xdev.ooms.sharedkernel.Enum.ExpenseCategory;
import  com.xdev.ooms.sharedkernel.Enum.ExpenseStatus;
import  com.xdev.ooms.sharedkernel.Enum.PaymentMethod;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDate;

import static org.apache.commons.math3.util.Precision.round;

@Entity
public class Expense extends BaseEntity implements Serializable {


    private String invoiceRef;

    private String purchaseNature;

    private String object;

    private LocalDate date;

    // Default to 0d for safety
    private Double amount = 0d;

    private String vendor;

    @Enumerated(EnumType.STRING)
    private ExpenseCategory category;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private String checkNumber;

    @Enumerated(EnumType.STRING)
    private ExpenseStatus status;

    private String notes;

    private String receiptNumber;

    // Default to FALSE for safety
    private Boolean approved = Boolean.FALSE;

    private LocalDate approvalDate;

    // ===== Getters / Setters =====

    public String getInvoiceRef() {
        return invoiceRef;
    }

    public void setInvoiceRef(String invoiceRef) {
        this.invoiceRef = invoiceRef;
    }

    public String getPurchaseNature() {
        return purchaseNature;
    }

    public void setPurchaseNature(String purchaseNature) {
        this.purchaseNature = purchaseNature;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        // null -> 0d and round to 3 decimals
        this.amount = (amount == null) ? 0d : round(amount, 3);
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public void setCategory(ExpenseCategory category) {
        this.category = category;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public ExpenseStatus getStatus() {
        return status;
    }

    public void setStatus(ExpenseStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        // null -> FALSE
        this.approved = (approved == null) ? Boolean.FALSE : approved;
    }

    public LocalDate getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(LocalDate approvalDate) {
        this.approvalDate = approvalDate;
    }
}
