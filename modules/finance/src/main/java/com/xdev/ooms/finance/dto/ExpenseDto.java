package com.xdev.ooms.finance.dto;

import  com.xdev.ooms.sharedkernel.Enum.ExpenseCategory;
import com.xdev.ooms.finance.model.Expense;
import  com.xdev.ooms.sharedkernel.Enum.PaymentMethod;
import  com.xdev.ooms.sharedkernel.Enum.ExpenseStatus;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

import java.time.LocalDate;

public class ExpenseDto extends BaseDto<Expense> {


    private String invoiceRef;
    private String purchaseNature;
    private String object;
    private LocalDate date;
    private Double amount;
    private String vendor;
    private     ExpenseCategory category;
    private PaymentMethod paymentMethod;
    private ExpenseStatus status;
    private String notes;
    private String receiptNumber;
    private String createdBy;
    private Boolean approved;
    private LocalDate approvalDate;
    private String checkNumber;

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
        this.amount = amount;
    }

    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }

    public ExpenseCategory getCategory() { return category; }
    public void setCategory(ExpenseCategory category) { this.category = category; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public ExpenseStatus getStatus() { return status; }
    public void setStatus(ExpenseStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }

    public LocalDate getApprovalDate() { return approvalDate; }
    public void setApprovalDate(LocalDate approvalDate) { this.approvalDate = approvalDate; }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }
}