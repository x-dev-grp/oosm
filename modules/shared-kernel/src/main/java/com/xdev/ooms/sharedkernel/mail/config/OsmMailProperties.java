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
