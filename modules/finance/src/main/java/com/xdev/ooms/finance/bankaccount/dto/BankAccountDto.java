package com.xdev.ooms.finance.bankaccount.dto;

import  com.xdev.ooms.sharedkernel.Enum.Currency;
import com.xdev.ooms.finance.bankaccount.entity.BankAccount;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;

public class BankAccountDto extends BaseDto<BankAccount> {
    private String bankName;
    private String rib;
    private String iban;
    private String bicSwift;
    private String bankBranch;
    private String accountType;
    private Boolean active;
    private Currency currency;

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

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