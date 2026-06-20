package com.xdev.ooms.security.notification.controller;

import com.xdev.ooms.security.notification.dto.UserNotificationDto;
import com.xdev.ooms.security.notification.service.UserNotificationService;
import com.xdev.ooms.sharedkernel.apiDTOs.ApiResponse;
import com.xdev.ooms.sharedkernel.apiDTOs.ApiSingleResponse;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import com.xdev.ooms.sharedkernel.utils.ExceptionHandler;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final UserNotificationService userNotificationService;

    public NotificationController(UserNotificationService userNotificationService) {
        this.userNotificationService = userNotificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<BaseEntity, UserNotificationDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        try {
            Page<UserNotificationDto> notifications = userNotificationService.listForCurrentUser(page, size, unreadOnly);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Notifications loaded",
                    notifications.getContent()));
        } catch (Exception ex) {
            return ExceptionHandler.handleException(this.getClass(), "list", ex);
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> unreadCount() {
        try {
            long count = userNotificationService.unreadCountForCurrentUser();
            return ResponseEntity.ok(Map.of("success", true, "count", count));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiSingleResponse<BaseEntity, UserNotificationDto>> markRead(@PathVariable UUID id) {
        try {
            UserNotificationDto dto = userNotificationService.markRead(id);
            return ResponseEntity.ok(new ApiSingleResponse<>(true, "Notification marked as read", dto));
        } catch (Exception ex) {
            return ExceptionHandler.handleSingleException(this.getClass(), "markRead", ex);
        }
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllRead() {
        try {
            long updated = userNotificationService.markAllReadForCurrentUser();
            return ResponseEntity.ok(Map.of("success", true, "updated", updated));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }
}
