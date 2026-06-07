package com.xdev.ooms.production.dto;


import com.xdev.ooms.sharedkernel.Enum.Currency;
import com.xdev.ooms.sharedkernel.Enum.PaymentMethod;
import com.xdev.ooms.sharedkernel.communicator.models.shared.BankAccountDto;
import com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto;

import java.util.UUID;

public class PaymentDTO {
    private UUID idOperation;
    private Double amount;
    private Currency currency;
    private PaymentMethod paymentMethod;

    private String checkNumber;
    private BankAccountDto bankAccount;
    private
    SupplierDto supplier;

    public UUID getIdOperation() {
        return idOperation;
    }

    public void setIdOperation(UUID idOperation) {
        this.idOperation = idOperation;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
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

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public BankAccountDto getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccountDto bankAccount) {
        this.bankAccount = bankAccount;
    }

    public SupplierDto getSupplier() {
        return supplier;
    }

    public void setSupplier(SupplierDto supplier) {
        this.supplier = supplier;
    }
}
