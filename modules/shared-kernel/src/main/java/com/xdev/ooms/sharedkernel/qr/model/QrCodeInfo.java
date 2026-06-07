package com.xdev.ooms.sharedkernel.qr.model;


public class QrCodeInfo   {
    private final String publicCode;
    private final String qrUrl;
    private final String qrImageBase64;  // new field

    public String getPublicCode() {
        return publicCode;
    }

    public QrCodeInfo(String publicCode, String qrUrl, String qrImageBase64) {
        this.publicCode = publicCode;
        this.qrUrl = qrUrl;
        this.qrImageBase64 = qrImageBase64;
    }

    public String getQrUrl() {
        return qrUrl;
    }

    public String getQrImageBase64() {
        return qrImageBase64;
    }
}
