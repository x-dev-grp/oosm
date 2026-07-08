package com.xdev.ooms.documents.commercial.dto;

import com.xdev.ooms.documents.commercial.BillVatMode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BillGenerationRequest {
    private String title = "Facture commerciale";
    private String invoiceNumber;
    private LocalDateTime operationDate;
    private String currency = "TND";
    private String conditions;
    private String logoBase64;
    private String logoContentType;
    private BillPartyDto issuer;
    private BillPartyDto client;
    private List<BillLineDto> lines = new ArrayList<>();
    private BillLogisticsDto logistics;
    private BillBankInfoDto bankInfo;
    private List<String> paymentTerms = new ArrayList<>();
    private BillFooterContactDto footerContact;
    private BigDecimal suspendedVatAmount;
    private String taxLegalMention;
    private String sourceType;
    private UUID sourceId;
    private String paymentMethod;
    private boolean electronicInvoice;
    private String ttnReference;
    private String issuerElectronicSeal;
    private String notes;
    private BillVatMode vatMode = BillVatMode.STANDARD;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public LocalDateTime getOperationDate() {
        return operationDate;
    }

    public void setOperationDate(LocalDateTime operationDate) {
        this.operationDate = operationDate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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

    public BillPartyDto getClient() {
        return client;
    }

    public void setClient(BillPartyDto client) {
        this.client = client;
    }

    public List<BillLineDto> getLines() {
        return lines;
    }

    public void setLines(List<BillLineDto> lines) {
        this.lines = lines;
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

    public BigDecimal getSuspendedVatAmount() {
        return suspendedVatAmount;
    }

    public void setSuspendedVatAmount(BigDecimal suspendedVatAmount) {
        this.suspendedVatAmount = suspendedVatAmount;
    }

    public String getTaxLegalMention() {
        return taxLegalMention;
    }

    public void setTaxLegalMention(String taxLegalMention) {
        this.taxLegalMention = taxLegalMention;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public void setSourceId(UUID sourceId) {
        this.sourceId = sourceId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
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

    public BillVatMode getVatMode() {
        return vatMode;
    }

    public void setVatMode(BillVatMode vatMode) {
        this.vatMode = vatMode;
    }
}
