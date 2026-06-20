package com.xdev.ooms.sharedkernel.ports;

import java.util.Map;
import java.util.UUID;

/**
 * Domain event payload for permission-scoped user notifications.
 */
public record NotificationEvent(
        String ruleCode,
        UUID entityId,
        String entityLabel,
        Map<String, String> recapFields,
        UUID actorUserId,
        String actorDisplayName
) {
    public NotificationEvent {
        recapFields = recapFields == null ? Map.of() : Map.copyOf(recapFields);
    }
}
