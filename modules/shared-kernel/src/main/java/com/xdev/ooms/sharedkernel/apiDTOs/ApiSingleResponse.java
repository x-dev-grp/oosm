package com.xdev.ooms.sharedkernel.apiDTOs;

import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;

public class ApiSingleResponse<E extends BaseEntity, OUTDTO extends BaseDto<E>> {

    private boolean success;
    private String message;
    private OUTDTO data;

    public ApiSingleResponse() {
    }

    public ApiSingleResponse(boolean success, String message, OUTDTO data) {
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

    public OUTDTO getData() {
        return data;
    }

    public void setData(OUTDTO data) {
        this.data = data;
    }
}
