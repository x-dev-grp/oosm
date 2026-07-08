package com.xdev.ooms.sharedkernel.settings.dto;

import java.util.UUID;

public class AdminSettingsActor {
    private final UUID userId;
    private final String username;
    private final String ipAddress;
    private final String userAgent;

    public AdminSettingsActor(UUID userId, String username, String ipAddress, String userAgent) {
        this.userId = userId;
        this.username = username;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }
}
