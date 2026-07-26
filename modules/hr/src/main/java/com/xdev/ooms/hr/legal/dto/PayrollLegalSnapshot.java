package com.xdev.ooms.hr.legal.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Snapshot of legal configuration versions used for a payroll run.
 */
public class PayrollLegalSnapshot {

    private UUID socialSecurityConfigId;
    private Integer socialSecurityVersion;
    private UUID taxConfigurationId;
    private Integer taxConfigurationVersion;
    private UUID minimumWageRuleId;
    private String minimumWageProfile;
    private UUID companyLegalProfileId;
    private LocalDate asOf;

    public UUID getSocialSecurityConfigId() {
        return socialSecurityConfigId;
    }

    public void setSocialSecurityConfigId(UUID socialSecurityConfigId) {
        this.socialSecurityConfigId = socialSecurityConfigId;
    }

    public Integer getSocialSecurityVersion() {
        return socialSecurityVersion;
    }

    public void setSocialSecurityVersion(Integer socialSecurityVersion) {
        this.socialSecurityVersion = socialSecurityVersion;
    }

    public UUID getTaxConfigurationId() {
        return taxConfigurationId;
    }

    public void setTaxConfigurationId(UUID taxConfigurationId) {
        this.taxConfigurationId = taxConfigurationId;
    }

    public Integer getTaxConfigurationVersion() {
        return taxConfigurationVersion;
    }

    public void setTaxConfigurationVersion(Integer taxConfigurationVersion) {
        this.taxConfigurationVersion = taxConfigurationVersion;
    }

    public UUID getMinimumWageRuleId() {
        return minimumWageRuleId;
    }

    public void setMinimumWageRuleId(UUID minimumWageRuleId) {
        this.minimumWageRuleId = minimumWageRuleId;
    }

    public String getMinimumWageProfile() {
        return minimumWageProfile;
    }

    public void setMinimumWageProfile(String minimumWageProfile) {
        this.minimumWageProfile = minimumWageProfile;
    }

    public UUID getCompanyLegalProfileId() {
        return companyLegalProfileId;
    }

    public void setCompanyLegalProfileId(UUID companyLegalProfileId) {
        this.companyLegalProfileId = companyLegalProfileId;
    }

    public LocalDate getAsOf() {
        return asOf;
    }

    public void setAsOf(LocalDate asOf) {
        this.asOf = asOf;
    }
}
