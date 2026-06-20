package com.xdev.ooms.sharedkernel.notifications.dto;

import java.util.List;
import java.util.Map;

public class NotificationRequest {

    private List<String> userIds;
    private String title;
    private String message;
    private Map<String, String> data;

    public NotificationRequest() {
    }

    public NotificationRequest(List<String> userIds, String title, String message, Map<String, String> data) {
        this.userIds = userIds;
        this.title = title;
        this.message = message;
        this.data = data;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
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

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
