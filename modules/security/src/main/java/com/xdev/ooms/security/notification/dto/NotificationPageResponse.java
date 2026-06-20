package com.xdev.ooms.security.notification.dto;

import java.util.List;

public class NotificationPageResponse {

    private boolean success;
    private String message;
    private List<UserNotificationDto> data;
    private long total;
    private int page;
    private int totalPages;
    private long unreadCount;

    public NotificationPageResponse() {
    }

    public NotificationPageResponse(
            boolean success,
            String message,
            List<UserNotificationDto> data,
            long total,
            int page,
            int totalPages,
            long unreadCount) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.total = total;
        this.page = page;
        this.totalPages = totalPages;
        this.unreadCount = unreadCount;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<UserNotificationDto> getData() {
        return data;
    }

    public void setData(List<UserNotificationDto> data) {
        this.data = data;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }
}
