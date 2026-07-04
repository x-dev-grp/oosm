package com.xdev.ooms.security.supportticket.dto;

import java.util.List;

public class SupportTicketPageResponse {

    private boolean success;
    private String message;
    private List<SupportTicketDto> data;
    private long total;
    private int page;
    private int totalPages;

    public SupportTicketPageResponse() {
    }

    public SupportTicketPageResponse(
            boolean success,
            String message,
            List<SupportTicketDto> data,
            long total,
            int page,
            int totalPages) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.total = total;
        this.page = page;
        this.totalPages = totalPages;
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

    public List<SupportTicketDto> getData() {
        return data;
    }

    public void setData(List<SupportTicketDto> data) {
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
}
