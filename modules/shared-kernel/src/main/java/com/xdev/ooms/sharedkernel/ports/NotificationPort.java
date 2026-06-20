package com.xdev.ooms.sharedkernel.ports;

/**
 * Cross-module port: publish in-app and push notifications to permission-scoped users.
 */
public interface NotificationPort {

    void publish(NotificationEvent event);
}
