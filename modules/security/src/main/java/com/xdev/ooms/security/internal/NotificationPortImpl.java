package com.xdev.ooms.security.internal;

import com.xdev.ooms.security.notification.service.NotificationDispatcher;
import com.xdev.ooms.sharedkernel.ports.NotificationEvent;
import com.xdev.ooms.sharedkernel.ports.NotificationPort;
import org.springframework.stereotype.Service;

@Service
public class NotificationPortImpl implements NotificationPort {

    private final NotificationDispatcher notificationDispatcher;

    public NotificationPortImpl(NotificationDispatcher notificationDispatcher) {
        this.notificationDispatcher = notificationDispatcher;
    }

    @Override
    public void publish(NotificationEvent event) {
        notificationDispatcher.dispatch(event);
    }
}
