package com.xdev.ooms.sharedkernel.mail.config;

public enum MailProvider {
    RESEND,
    SMTP;

    public static MailProvider from(String value) {
        if (value != null && "SMTP".equalsIgnoreCase(value.trim())) {
            return SMTP;
        }
        return RESEND;
    }
}
