package com.xdev.ooms.security.notification.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xdev.ooms.security.notification.dto.UserNotificationDto;
import com.xdev.ooms.security.notification.entity.UserNotification;
import com.xdev.ooms.security.notification.repository.UserNotificationRepository;
import com.xdev.ooms.security.user.entity.OSMUser;
import com.xdev.ooms.security.user.repository.UserRepository;
import com.xdev.ooms.sharedkernel.utils.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Service
public class UserNotificationService {

    private final UserNotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public UserNotificationService(
            UserNotificationRepository notificationRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public Page<UserNotificationDto> listForCurrentUser(int page, int size, boolean unreadOnly) {
        UUID userId = requireCurrentUserId();
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        Page<UserNotification> result = unreadOnly
                ? notificationRepository.findByUserIdAndReadAtIsNullAndIsDeletedFalseOrderByCreatedDateDesc(userId, pageable)
                : notificationRepository.findByUserIdAndIsDeletedFalseOrderByCreatedDateDesc(userId, pageable);
        return result.map(this::toDto);
    }

    public long unreadCountForCurrentUser() {
        UUID userId = requireCurrentUserId();
        return notificationRepository.countByUserIdAndReadAtIsNullAndIsDeletedFalse(userId);
    }

    @Transactional
    public UserNotificationDto markRead(UUID notificationId) {
        UUID userId = requireCurrentUserId();
        UserNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
        if (!userId.equals(notification.getUserId())) {
            throw new EntityNotFoundException("Notification not found");
        }
        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
        return toDto(notification);
    }

    @Transactional
    public long markAllReadForCurrentUser() {
        UUID userId = requireCurrentUserId();
        return notificationRepository.markAllReadForUser(userId, LocalDateTime.now());
    }

    private UUID requireCurrentUserId() {
        return SecurityUtils.getCurrentUserId()
                .orElseGet(() -> userRepository.findByUsername(SecurityUtils.getCurrentUsername().orElse(""))
                        .map(OSMUser::getId)
                        .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found")));
    }

    private UserNotificationDto toDto(UserNotification notification) {
        UserNotificationDto dto = new UserNotificationDto();
        dto.setId(notification.getId());
        dto.setTenantId(notification.getTenantId());
        dto.setRuleCode(notification.getRuleCode());
        dto.setModule(notification.getModule() != null ? notification.getModule().name() : null);
        dto.setEntity(notification.getEntity());
        dto.setEntityId(notification.getEntityId());
        dto.setTitle(notification.getTitle());
        dto.setRecap(notification.getRecap());
        dto.setPriority(notification.getPriority());
        dto.setActorDisplayName(notification.getActorDisplayName());
        dto.setRead(notification.getReadAt() != null);
        dto.setCreatedDate(notification.getCreatedDate());
        dto.setReadAt(notification.getReadAt());

        Map<String, String> payload = parsePayload(notification.getPayloadJson());
        dto.setPayload(payload);
        dto.setWebRoute(payload.getOrDefault("webRoute", "/"));
        return dto;
    }

    private Map<String, String> parsePayload(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(payloadJson, new TypeReference<>() {});
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }
}
