package com.xdev.ooms.sharedkernel.mail.config;

import com.xdev.ooms.sharedkernel.settings.service.AppSettingsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Component
public class DynamicMailSettings {

    private final AppSettingsService appSettingsService;

    public DynamicMailSettings(AppSettingsService appSettingsService) {
        this.appSettingsService = appSettingsService;
    }

    public MailProvider getProvider() {
        return MailProvider.from(appSettingsService.getString("MAIL_PROVIDER", "RESEND"));
    }

    public boolean isEnabled() {
        return appSettingsService.getBoolean("MAIL_ENABLED", true);
    }

    public boolean isDeliveryEnabled() {
        if (!isEnabled() || !StringUtils.hasText(getFromAddress())) {
            return false;
        }
        if (getProvider() == MailProvider.SMTP) {
            return isSmtpConfigured();
        }
        return getApiKey().isPresent();
    }

    public Optional<String> getApiKey() {
        return appSettingsService.getSecret("RESEND_API_KEY");
    }

    public String getFromAddress() {
        return appSettingsService.getString("MAIL_FROM_ADDRESS", "");
    }

    public String getFromName() {
        return appSettingsService.getString("MAIL_FROM_NAME", "ZitFlow");
    }

    public String getFormattedFrom() {
        String fromAddress = getFromAddress();
        String fromName = getFromName();
        if (!StringUtils.hasText(fromName)) {
            return fromAddress;
        }
        return fromName + " <" + fromAddress + ">";
    }

    public String getSupportEmail() {
        String supportEmail = appSettingsService.getString("MAIL_SUPPORT_EMAIL", "");
        if (StringUtils.hasText(supportEmail)) {
            return supportEmail;
        }
        return getFromAddress();
    }

    public String getFrontendBaseUrl() {
        return appSettingsService.getString("FRONTEND_ENTRY_POINT", "http://localhost:4200");
    }

    public boolean isDebug() {
        return appSettingsService.getBoolean("MAIL_DEBUG", false);
    }

    public String getSmtpHost() {
        return appSettingsService.getString("SMTP_HOST", "");
    }

    public int getSmtpPort() {
        String raw = appSettingsService.getString("SMTP_PORT", "587");
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return 587;
        }
    }

    public String getSmtpUsername() {
        return appSettingsService.getString("SMTP_USERNAME", "");
    }

    public Optional<String> getSmtpPassword() {
        return appSettingsService.getSecret("SMTP_PASSWORD");
    }

    public boolean isSmtpAuth() {
        return appSettingsService.getBoolean("SMTP_AUTH", true);
    }

    public boolean isSmtpStartTls() {
        return appSettingsService.getBoolean("SMTP_STARTTLS", true);
    }

    private boolean isSmtpConfigured() {
        if (!StringUtils.hasText(getSmtpHost()) || getSmtpPort() <= 0) {
            return false;
        }
        if (!isSmtpAuth()) {
            return true;
        }
        return StringUtils.hasText(getSmtpUsername()) && getSmtpPassword().isPresent();
    }
}
