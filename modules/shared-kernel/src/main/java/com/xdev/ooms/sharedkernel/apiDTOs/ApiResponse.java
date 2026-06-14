package com.xdev.ooms.sharedkernel.apiDTOs;

import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;

import java.util.List;

public class ApiResponse<E extends BaseEntity, OUTDTO extends BaseDto<E>> {

    private boolean success;
    private String message;
    private List<OUTDTO> data;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String message, List<OUTDTO> data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Getters and setters


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

    public List<OUTDTO> getData() {
        return data;
    }

    public void setData(List<OUTDTO> data) {
        this.data = data;
    }
}
