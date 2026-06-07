package com.xdev.ooms.sharedkernel.notifications.impl;

import com.xdev.ooms.sharedkernel.notifications.dto.NotificationRequest;

public interface OneSignalServiceImpl {
    String sendNotification(NotificationRequest notificationRequest) ;
}
