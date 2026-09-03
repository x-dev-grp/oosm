package com.xdev.ooms.security.notification.service;

import com.xdev.ooms.sharedkernel.utils.OOSMLogger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xdev.ooms.security.notification.catalog.NotificationRulesLoader;
import com.xdev.ooms.security.notification.catalog.NotificationRulesSpec;
import com.xdev.ooms.security.notification.entity.UserNotification;
import com.xdev.ooms.security.notification.repository.UserNotificationRepository;
import com.xdev.ooms.security.user.entity.OOSMUser;
import com.xdev.ooms.security.user.repository.UserRepository;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.models.OOSMModule;
import com.xdev.ooms.sharedkernel.notifications.PushNotificationSender;
import com.xdev.ooms.sharedkernel.notifications.FcmPushService;
import com.xdev.ooms.sharedkernel.notifications.dto.NotificationRequest;
import com.xdev.ooms.sharedkernel.ports.NotificationEvent;
import com.xdev.ooms.sharedkernel.settings.service.AppSettingsService;
import com.xdev.ooms.sharedkernel.utils.AuditHelper;
import com.xdev.ooms.sharedkernel.utils.SecurityUtils;
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

    private final NotificationRulesLoader rulesLoader;
    private final UserRepository userRepository;
    private final UserNotificationRepository notificationRepository;
    private final NotificationMessageBuilder messageBuilder;
    private final PushNotificationSender pushNotificationSender;
    private final ObjectMapper objectMapper;
    private final AppSettingsService appSettingsService;

    public NotificationDispatcher(
            NotificationRulesLoader rulesLoader,
            UserRepository userRepository,
            UserNotificationRepository notificationRepository,
            NotificationMessageBuilder messageBuilder,
            PushNotificationSender pushNotificationSender,
            ObjectMapper objectMapper,
            AppSettingsService appSettingsService) {
        this.rulesLoader = rulesLoader;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.messageBuilder = messageBuilder;
        this.pushNotificationSender = pushNotificationSender;
        this.objectMapper = objectMapper;
        this.appSettingsService = appSettingsService;
    }

    @Transactional
    public void dispatch(NotificationEvent event) {
        if (event == null || event.ruleCode() == null || event.ruleCode().isBlank()) {
            return;
        }

        NotificationRulesSpec.NotificationRuleSpec rule = rulesLoader.getRule(event.ruleCode());
        if (rule == null) {
            OOSMLogger.warn(NotificationDispatcher.class, "Unknown notification rule: {}", event.ruleCode());
            return;
        }

        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            OOSMLogger.warn(NotificationDispatcher.class, "Skipping notification {} because tenant is missing", event.ruleCode());
            return;
        }

        OOSMModule module = OOSMModule.valueOf(rule.getModule().trim().toUpperCase());
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

        List<OOSMUser> recipients = userRepository.findAssignableUsersByPermissionOrAdmin(
                tenantId, module, entity, recipientAction);

        Set<UUID> recipientIds = recipients.stream()
                .map(OOSMUser::getId)
                .filter(id -> actorUserId == null || !actorUserId.equals(id))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (recipientIds.isEmpty()) {
            OOSMLogger.warn(NotificationDispatcher.class,
                    "No recipients for notification rule {} (module={}, entity={}, action={}, tenant={})",
                    event.ruleCode(),
                    module,
                    entity,
                    recipientAction,
                    tenantId);
            return;
        }

        OOSMLogger.info(NotificationDispatcher.class,
                "Dispatching notification {} to {} recipient(s) (tenant={}, entityId={})",
                event.ruleCode(),
                recipientIds.size(),
                tenantId,
                event.entityId());

        String title = rule.getTitle();
        String recap = messageBuilder.buildRecap(rule, event, actorDisplayName);
        String webRoute = messageBuilder.buildRoute(rule, event);
        Map<String, String> payload = buildPayload(event, webRoute);

        List<String> deviceTokens = new ArrayList<>();
        List<UserNotification> notificationsToSave = new ArrayList<>();
        boolean fcmConfigured = appSettingsService.getSecret(FcmPushService.FCM_PRIVATE_KEY).isPresent()
                && org.springframework.util.StringUtils.hasText(appSettingsService.getString(FcmPushService.FCM_PROJECT_ID, ""))
                && org.springframework.util.StringUtils.hasText(appSettingsService.getString(FcmPushService.FCM_CLIENT_EMAIL, ""));
        for (OOSMUser user : recipients) {
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
            notificationsToSave.add(notification);

            if (rule.isPushEnabled()
                    && fcmConfigured
                    && user.getFcmToken() != null
                    && !user.getFcmToken().isBlank()) {
                deviceTokens.add(user.getFcmToken());
            }
        }

        if (!notificationsToSave.isEmpty()) {
            notificationRepository.saveAll(notificationsToSave);
        }

        if (!deviceTokens.isEmpty()) {
            sendPush(deviceTokens, title, recap, payload);
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

    private void sendPush(List<String> deviceTokens, String title, String recap, Map<String, String> payload) {
        try {
            NotificationRequest request = new NotificationRequest(deviceTokens, title, recap, payload);
            pushNotificationSender.sendNotification(request);
        } catch (Exception ex) {
            OOSMLogger.warn(NotificationDispatcher.class, "Push notification failed: {}", ex.getMessage());
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
