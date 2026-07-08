package com.xdev.ooms.sharedkernel.settings.dto;

public class PublicObservabilityConfigDto {

    private boolean newRelicBrowserEnabled;
    private String accountId;
    private String applicationId;
    private String licenseKey;
    private String trustKey;

    public boolean isNewRelicBrowserEnabled() {
        return newRelicBrowserEnabled;
    }

    public void setNewRelicBrowserEnabled(boolean newRelicBrowserEnabled) {
        this.newRelicBrowserEnabled = newRelicBrowserEnabled;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }

    public String getTrustKey() {
        return trustKey;
    }

    public void setTrustKey(String trustKey) {
        this.trustKey = trustKey;
    }
}
