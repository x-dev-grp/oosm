package com.xdev.ooms.sharedkernel.mail.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OsmMailProperties {

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Value("${app.mail.from-name:OSM}")
    private String fromName;

    @Value("${app.mail.support-email:${spring.mail.username:}}")
    private String supportEmail;

    @Value("${app.frontend.base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    @Value("${app.mail.debug:false}")
    private boolean debug;

    @Value("${app.mail.enabled:true}")
    private boolean enabled;

    @Value("${spring.mail.host:}")
    private String host;

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isDeliveryEnabled() {
        return enabled && host != null && !host.isBlank()
                && fromAddress != null && !fromAddress.isBlank();
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public String getFromName() {
        return fromName;
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
}
