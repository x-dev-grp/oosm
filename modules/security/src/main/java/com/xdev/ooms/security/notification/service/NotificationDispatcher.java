package com.xdev.ooms.security.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xdev.ooms.security.notification.catalog.NotificationRulesLoader;
import com.xdev.ooms.security.notification.catalog.NotificationRulesSpec;
import com.xdev.ooms.security.notification.entity.UserNotification;
import com.xdev.ooms.security.notification.repository.UserNotificationRepository;
import com.xdev.ooms.security.user.entity.OSMUser;
import com.xdev.ooms.security.user.repository.UserRepository;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.models.OSMModule;
import com.xdev.ooms.sharedkernel.notifications.dto.NotificationRequest;
import com.xdev.ooms.sharedkernel.notifications.impl.OneSignalServiceImpl;
import com.xdev.ooms.sharedkernel.ports.NotificationEvent;
import com.xdev.ooms.sharedkernel.utils.AuditHelper;
import com.xdev.ooms.sharedkernel.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);

    private final NotificationRulesLoader rulesLoader;
    private final UserRepository userRepository;
    private final UserNotificationRepository notificationRepository;
    private final NotificationMessageBuilder messageBuilder;
    private final OneSignalServiceImpl oneSignalService;
    private final ObjectMapper objectMapper;

    @Value("${onesignal.app-id:}")
    private String oneSignalAppId;

    public NotificationDispatcher(
            NotificationRulesLoader rulesLoader,
            UserRepository userRepository,
            UserNotificationRepository notificationRepository,
            NotificationMessageBuilder messageBuilder,
            OneSignalServiceImpl oneSignalService,
            ObjectMapper objectMapper) {
        this.rulesLoader = rulesLoader;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.messageBuilder = messageBuilder;
        this.oneSignalService = oneSignalService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void dispatch(NotificationEvent event) {
        if (event == null || event.ruleCode() == null || event.ruleCode().isBlank()) {
            return;
        }

        NotificationRulesSpec.NotificationRuleSpec rule = rulesLoader.getRule(event.ruleCode());
        if (rule == null) {
            log.warn("Unknown notification rule: {}", event.ruleCode());
            return;
        }

        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            log.debug("Skipping notification {} because tenant is missing", event.ruleCode());
            return;
        }

        OSMModule module = OSMModule.valueOf(rule.getModule().trim().toUpperCase());
        String entity = rule.getEntity().trim().toUpperCase();
        String recipientAction = rule.getRecipientAction() != null
                ? rule.getRecipientAction().trim().toUpperCase()
                : "READ";

        UUID actorUserId = event.actorUserId() != null
                ? event.actorUserId()
                : SecurityUtils.getCurrentUserId().orElse(null);
        String actorDisplayName = event.actorDisplayName() != null && !event.actorDisplayName().isBlank()
                ? event.actorDisplayName()
                : SecurityUtils.getCurrentUserDisplayName().orElse("System");

        List<OSMUser> recipients = userRepository.findAssignableUsersByPermissionOrAdmin(
                tenantId, module, entity, recipientAction);

        Set<UUID> recipientIds = recipients.stream()
                .map(OSMUser::getId)
                .filter(id -> actorUserId == null || !actorUserId.equals(id))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (recipientIds.isEmpty()) {
            log.debug("No recipients for notification rule {}", event.ruleCode());
            return;
        }

        String title = rule.getTitle();
        String recap = messageBuilder.buildRecap(rule, event, actorDisplayName);
        String webRoute = messageBuilder.buildRoute(rule, event);
        Map<String, String> payload = buildPayload(event, webRoute);

        List<String> playerIds = new ArrayList<>();
        for (OSMUser user : recipients) {
            if (!recipientIds.contains(user.getId())) {
                continue;
            }
            UserNotification notification = new UserNotification();
            notification.setUserId(user.getId());
            notification.setTenantId(tenantId);
            notification.setRuleCode(event.ruleCode());
            notification.setModule(module);
            notification.setEntity(entity);
            notification.setAction(recipientAction);
            notification.setEntityId(event.entityId());
            notification.setTitle(title);
            notification.setRecap(recap);
            notification.setPayloadJson(toJson(payload));
            notification.setPriority(rule.getPriority() != null ? rule.getPriority() : "NORMAL");
            notification.setActorUserId(actorUserId);
            notification.setActorDisplayName(actorDisplayName);
            AuditHelper.applyAuditOnCreate(notification);
            notificationRepository.save(notification);

            if (rule.isPushEnabled()
                    && oneSignalAppId != null
                    && !oneSignalAppId.isBlank()
                    && user.getOneSignalPlayerId() != null
                    && !user.getOneSignalPlayerId().isBlank()) {
                playerIds.add(user.getOneSignalPlayerId());
            }
        }

        if (!playerIds.isEmpty()) {
            sendPush(playerIds, title, recap, payload);
        }
    }

    private Map<String, String> buildPayload(NotificationEvent event, String webRoute) {
        Map<String, String> payload = new HashMap<>();
        payload.put("ruleCode", event.ruleCode());
        payload.put("webRoute", webRoute);
        if (event.entityId() != null) {
            payload.put("entityId", event.entityId().toString());
        }
        if (event.entityLabel() != null) {
            payload.put("entityLabel", event.entityLabel());
        }
        if (event.recapFields() != null) {
            payload.putAll(event.recapFields());
        }
        return payload;
    }

    private void sendPush(List<String> playerIds, String title, String recap, Map<String, String> payload) {
        try {
            NotificationRequest request = new NotificationRequest(playerIds, title, recap, payload);
            oneSignalService.sendNotification(request);
        } catch (Exception ex) {
            log.warn("Push notification failed: {}", ex.getMessage());
        }
    }

    private String toJson(Map<String, String> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }
}
