package com.xdev.ooms.security.companyprofile.entity;

import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Basic;
import jakarta.persistence.FetchType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class CompanyProfile extends BaseEntity implements Serializable {

    private String legalName;

    private String registrationNumber;

    private String taxId;

    private String cnssNumber;

    private String legalForm;
    private boolean active=false;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

//   ( precision = 19, scale = 2)
    private BigDecimal capital;


   
    private String email;

   
    private String phone;
   
    private String website;
    private String addressLine1;
   
    private String city;
    private String postalCode;
   
    private String governorate;

    @Column(name = "campaign_start_at")
    private LocalDateTime campaignStartAt;

    @Column(name = "campaign_end_at")
    private LocalDateTime campaignEndAt;

    @Column(name = "campaign_start_month")
    private Integer campaignStartMonth = 9;

    @Column(name = "campaign_start_day")
    private Integer campaignStartDay = 1;

    @Column(name = "campaign_end_month")
    private Integer campaignEndMonth = 4;

    @Column(name = "campaign_end_day")
    private Integer campaignEndDay = 30;
    /**
     * Logo binary data, max ~200KB enforced by service/controller
     */
    @Column(columnDefinition = "TEXT")
    private String logoData;

    @Column(length = 50)
    private String logoContentType;

    @Column(name = "creation_date")
    private java.time.LocalDate creationDate;

    @Column(name = "invoice_footer_note", columnDefinition = "TEXT")
    private String invoiceFooterNote;

    @Column(name = "invoice_legal_mentions", columnDefinition = "TEXT")
    private String invoiceLegalMentions;

    @Column(name = "preferred_theme_color", length = 40)
    private String preferredThemeColor;

    @Column(name = "default_language", length = 10)
    private String defaultLanguage;

    @Column(name = "timezone", length = 60)
    private String timezone;

    @Column(name = "pwa_short_name", length = 40)
    private String pwaShortName;

    @Column(name = "invoice_bank_name", length = 120)
    private String invoiceBankName;

    @Column(name = "invoice_bank_iban", length = 64)
    private String invoiceBankIban;

    @Column(name = "invoice_bank_swift", length = 32)
    private String invoiceBankSwift;


    public String getLogoData() {
        return logoData;
    }

    public void setLogoData(String logoData) {
        this.logoData = logoData;
    }

    public String getLogoContentType() {
        return logoContentType;
    }

    public void setLogoContentType(String logoContentType) {
        this.logoContentType = logoContentType;
    }

    public String getLegalName() {
        return legalName;
    }
    // --- Getters & Setters ---

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getCnssNumber() {
        return cnssNumber;
    }

    public void setCnssNumber(String cnssNumber) {
        this.cnssNumber = cnssNumber;
    }

    public String getLegalForm() {
        return legalForm;
    }

    public void setLegalForm(String legalForm) {
        this.legalForm = legalForm;
    }

    public BigDecimal getCapital() {
        return capital;
    }

    public void setCapital(BigDecimal capital) {
        this.capital = capital;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getGovernorate() {
        return governorate;
    }

    public void setGovernorate(String governorate) {
        this.governorate = governorate;
    }

    public LocalDateTime getCampaignStartAt() {
        return campaignStartAt;
    }

    public void setCampaignStartAt(LocalDateTime campaignStartAt) {
        this.campaignStartAt = campaignStartAt;
    }

    public LocalDateTime getCampaignEndAt() {
        return campaignEndAt;
    }

    public void setCampaignEndAt(LocalDateTime campaignEndAt) {
        this.campaignEndAt = campaignEndAt;
    }

    public Integer getCampaignStartMonth() {
        return campaignStartMonth;
    }

    public void setCampaignStartMonth(Integer campaignStartMonth) {
        this.campaignStartMonth = campaignStartMonth;
    }

    public Integer getCampaignStartDay() {
        return campaignStartDay;
    }

    public void setCampaignStartDay(Integer campaignStartDay) {
        this.campaignStartDay = campaignStartDay;
    }

    public Integer getCampaignEndMonth() {
        return campaignEndMonth;
    }

    public void setCampaignEndMonth(Integer campaignEndMonth) {
        this.campaignEndMonth = campaignEndMonth;
    }

    public Integer getCampaignEndDay() {
        return campaignEndDay;
    }

    public void setCampaignEndDay(Integer campaignEndDay) {
        this.campaignEndDay = campaignEndDay;
    }

    public java.time.LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(java.time.LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public String getInvoiceFooterNote() {
        return invoiceFooterNote;
    }

    public void setInvoiceFooterNote(String invoiceFooterNote) {
        this.invoiceFooterNote = invoiceFooterNote;
    }

    public String getInvoiceLegalMentions() {
        return invoiceLegalMentions;
    }

    public void setInvoiceLegalMentions(String invoiceLegalMentions) {
        this.invoiceLegalMentions = invoiceLegalMentions;
    }

    public String getPreferredThemeColor() {
        return preferredThemeColor;
    }

    public void setPreferredThemeColor(String preferredThemeColor) {
        this.preferredThemeColor = preferredThemeColor;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getPwaShortName() {
        return pwaShortName;
    }

    public void setPwaShortName(String pwaShortName) {
        this.pwaShortName = pwaShortName;
    }

    public String getInvoiceBankName() {
        return invoiceBankName;
    }

    public void setInvoiceBankName(String invoiceBankName) {
        this.invoiceBankName = invoiceBankName;
    }

    public String getInvoiceBankIban() {
        return invoiceBankIban;
    }

    public void setInvoiceBankIban(String invoiceBankIban) {
        this.invoiceBankIban = invoiceBankIban;
    }

    public String getInvoiceBankSwift() {
        return invoiceBankSwift;
    }

    public void setInvoiceBankSwift(String invoiceBankSwift) {
        this.invoiceBankSwift = invoiceBankSwift;
    }


}
