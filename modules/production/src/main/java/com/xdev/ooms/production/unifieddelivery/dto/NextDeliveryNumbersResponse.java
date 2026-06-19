package com.xdev.ooms.production.unifieddelivery.dto;

public class NextDeliveryNumbersResponse {
    private final boolean success;
    private final String message;
    private final NextDeliveryNumbersDto data;

    public NextDeliveryNumbersResponse(boolean success, String message, NextDeliveryNumbersDto data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public NextDeliveryNumbersDto getData() {
        return data;
    }
}
