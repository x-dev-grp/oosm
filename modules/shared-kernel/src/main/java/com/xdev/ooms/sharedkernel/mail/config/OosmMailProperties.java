package com.xdev.ooms.sharedkernel.mail.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class OosmMailProperties {

    @Value("${app.mail.from-address:}")
    private String fromAddress;

    @Value("${app.mail.from-name:OOSM}")
    private String fromName;

    @Value("${app.mail.support-email:${app.mail.from-address:}}")
    private String supportEmail;

    @Value("${app.frontend.base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    @Value("${app.mail.debug:false}")
    private boolean debug;

    @Value("${app.mail.enabled:true}")
    private boolean enabled;

    @Value("${app.mail.resend.api-key:}")
    private String apiKey;

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isDeliveryEnabled() {
        return enabled
                && StringUtils.hasText(apiKey)
                && StringUtils.hasText(fromAddress);
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public String getFromName() {
        return fromName;
    }

    public String getFormattedFrom() {
        if (!StringUtils.hasText(fromName)) {
            return fromAddress;
        }
        return fromName + " <" + fromAddress + ">";
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public String getFrontendBaseUrl() {
        return frontendBaseUrl;
    }

    public boolean isDebug() {
        return debug;
    }

    public String getApiKey() {
        return apiKey;
    }
}
