package com.xdev.ooms.sharedkernel.notifications;

import com.xdev.ooms.sharedkernel.notifications.dto.NotificationRequest;

/**
 * Transport for outbound push notifications (FCM HTTP v1).
 */
public interface PushNotificationSender {
    String sendNotification(NotificationRequest notificationRequest);
}
