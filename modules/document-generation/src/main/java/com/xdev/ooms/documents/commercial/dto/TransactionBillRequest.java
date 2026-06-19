package com.xdev.ooms.documents.commercial.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TransactionBillRequest {
    private String title;
    private String conditions;
    private String logoBase64;
    private String logoContentType;
    private BillPartyDto issuer;
    private BillPartyDto clientOverride;
    private String designation;
    private BigDecimal vatRatePercent = BigDecimal.ZERO;
    private BillLogisticsDto logistics;
    private BillBankInfoDto bankInfo;
    private List<String> paymentTerms = new ArrayList<>();
    private BillFooterContactDto footerContact;
    private boolean electronicInvoice;
    private String ttnReference;
    private String issuerElectronicSeal;
    private String notes;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public String getLogoBase64() {
        return logoBase64;
    }

    public void setLogoBase64(String logoBase64) {
        this.logoBase64 = logoBase64;
    }

    public String getLogoContentType() {
        return logoContentType;
    }

    public void setLogoContentType(String logoContentType) {
        this.logoContentType = logoContentType;
    }

    public BillPartyDto getIssuer() {
        return issuer;
    }

    public void setIssuer(BillPartyDto issuer) {
        this.issuer = issuer;
    }

    public BillPartyDto getClientOverride() {
        return clientOverride;
    }

    public void setClientOverride(BillPartyDto clientOverride) {
        this.clientOverride = clientOverride;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public BigDecimal getVatRatePercent() {
        return vatRatePercent;
    }

    public void setVatRatePercent(BigDecimal vatRatePercent) {
        this.vatRatePercent = vatRatePercent;
    }

    public BillLogisticsDto getLogistics() {
        return logistics;
    }

    public void setLogistics(BillLogisticsDto logistics) {
        this.logistics = logistics;
    }

    public BillBankInfoDto getBankInfo() {
        return bankInfo;
    }

    public void setBankInfo(BillBankInfoDto bankInfo) {
        this.bankInfo = bankInfo;
    }

    public List<String> getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(List<String> paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public BillFooterContactDto getFooterContact() {
        return footerContact;
    }

    public void setFooterContact(BillFooterContactDto footerContact) {
        this.footerContact = footerContact;
    }

    public boolean isElectronicInvoice() {
        return electronicInvoice;
    }

    public void setElectronicInvoice(boolean electronicInvoice) {
        this.electronicInvoice = electronicInvoice;
    }

    public String getTtnReference() {
        return ttnReference;
    }

    public void setTtnReference(String ttnReference) {
        this.ttnReference = ttnReference;
    }

    public String getIssuerElectronicSeal() {
        return issuerElectronicSeal;
    }

    public void setIssuerElectronicSeal(String issuerElectronicSeal) {
        this.issuerElectronicSeal = issuerElectronicSeal;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
