package com.xdev.ooms.security.notification.service;

import com.xdev.ooms.security.notification.catalog.NotificationRulesSpec;
import com.xdev.ooms.sharedkernel.ports.NotificationEvent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class NotificationMessageBuilder {

    public String buildRecap(
            NotificationRulesSpec.NotificationRuleSpec rule,
            NotificationEvent event,
            String actorDisplayName) {
        String template = rule.getRecapTemplate();
        if (template == null || template.isBlank()) {
            return rule.getTitle();
        }

        Map<String, String> values = new HashMap<>();
        if (event.recapFields() != null) {
            values.putAll(event.recapFields());
        }
        values.put("actor", actorDisplayName != null ? actorDisplayName : "System");
        values.put("entityLabel", event.entityLabel() != null ? event.entityLabel() : "");
        if (event.entityId() != null) {
            values.put("entityId", event.entityId().toString());
        }

        String recap = template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            recap = recap.replace("{" + entry.getKey() + "}", entry.getValue() != null ? entry.getValue() : "");
        }
        return recap;
    }

    public String buildRoute(
            NotificationRulesSpec.NotificationRuleSpec rule,
            NotificationEvent event) {
        String template = rule.getRouteTemplate();
        if (template == null || template.isBlank()) {
            return "/";
        }
        String route = template;
        if (event.entityId() != null) {
            route = route.replace("{entityId}", event.entityId().toString());
        }
        route = route.replace("{entityLabel}", event.entityLabel() != null ? event.entityLabel() : "");
        return route;
    }
}
