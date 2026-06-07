package com.xdev.ooms.finance.model;

 import  com.xdev.ooms.sharedkernel.Enum.Currency;
 import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
public class BankAccount extends BaseEntity implements Serializable {

    @Column(nullable = false, length = 34)
    private String rib;

    @Column(nullable = false, length = 34)
    private String iban;

    @Column(nullable = false, length = 11)
    private String bicSwift;

    @Column(nullable = false)
    private String bankName;

    private String bankBranch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(nullable = false)
    private String accountType;  // "Current", "Savings", …


    @Column(nullable = false)
    private Boolean active;

    public String getRib() {
        return rib;
    }

    public void setRib(String rib) {
        this.rib = rib;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getBicSwift() {
        return bicSwift;
    }

    public void setBicSwift(String bicSwift) {
        this.bicSwift = bicSwift;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankBranch() {
        return bankBranch;
    }

    public void setBankBranch(String bankBranch) {
        this.bankBranch = bankBranch;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }


    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }


}
