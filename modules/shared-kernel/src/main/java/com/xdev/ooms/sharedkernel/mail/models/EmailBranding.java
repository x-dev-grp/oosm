package com.xdev.ooms.sharedkernel.mail.models;

public class EmailBranding {

    private String companyName;
    private String supportEmail;
    private String frontendBaseUrl;
    private String productName;

    public static EmailBranding defaults(String productName, String frontendBaseUrl, String supportEmail) {
        EmailBranding branding = new EmailBranding();
        branding.setProductName(productName);
        branding.setCompanyName(productName);
        branding.setFrontendBaseUrl(trimTrailingSlash(frontendBaseUrl));
        branding.setSupportEmail(supportEmail);
        return branding;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public void setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
    }

    public String getFrontendBaseUrl() {
        return frontendBaseUrl;
    }

    public void setFrontendBaseUrl(String frontendBaseUrl) {
        this.frontendBaseUrl = trimTrailingSlash(frontendBaseUrl);
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String loginUrl() {
        return frontendBaseUrl + "/auth/login";
    }

    public String resetUrl(String userId) {
        return frontendBaseUrl + "/auth/reset/" + userId;
    }

    private static String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
