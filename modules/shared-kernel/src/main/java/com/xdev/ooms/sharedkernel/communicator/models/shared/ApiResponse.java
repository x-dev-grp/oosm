package com.xdev.ooms.sharedkernel.communicator.models.shared;

/**
 * Simple API wrapper for payloads that are not bound to {@link com.xdev.ooms.sharedkernel.entities.BaseEntity}.
 * Entity CRUD controllers should use {@link com.xdev.ooms.sharedkernel.apiDTOs.ApiResponse} instead.
 */
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
