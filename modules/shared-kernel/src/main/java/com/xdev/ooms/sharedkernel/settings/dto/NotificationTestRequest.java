package com.xdev.ooms.sharedkernel.settings.dto;

import org.springframework.util.StringUtils;

public class NotificationTestRequest {
    /** Preferred FCM device token field. */
    private String token;
    /** Legacy alias accepted for one release. */
    private String playerId;
    private String title;
    private String message;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String resolveDeviceToken() {
        if (StringUtils.hasText(token)) {
            return token.trim();
        }
        if (StringUtils.hasText(playerId)) {
            return playerId.trim();
        }
        return "";
    }
}
