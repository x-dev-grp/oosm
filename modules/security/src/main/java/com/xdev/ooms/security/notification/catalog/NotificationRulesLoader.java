package com.xdev.ooms.security.notification.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class NotificationRulesLoader {

    public static final String SPEC_RESOURCE = "notifications/notification-rules-spec.json";

    private final ObjectMapper objectMapper;
    private NotificationRulesSpec cached;

    public NotificationRulesLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public NotificationRulesSpec loadSpec() throws IOException {
        if (cached != null) {
            return cached;
        }
        ClassPathResource resource = new ClassPathResource(SPEC_RESOURCE);
        try (InputStream in = resource.getInputStream()) {
            cached = objectMapper.readValue(in, NotificationRulesSpec.class);
            return cached;
        }
    }

    public NotificationRulesSpec.NotificationRuleSpec getRule(String ruleCode) {
        try {
            NotificationRulesSpec spec = loadSpec();
            if (spec.getRules() == null) {
                return null;
            }
            return spec.getRules().get(ruleCode);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load notification rules catalog", ex);
        }
    }
}
