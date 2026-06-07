package com.xdev.ooms.sharedkernel.communicator.models.shared;

import java.io.Serializable;

public class LabelValidationIssueDto implements Serializable {
    private String field;
    private String message;
    private boolean blocking;



    public LabelValidationIssueDto(String field, String message, boolean blocking) {
        this.field = field;
        this.message = message;
        this.blocking = blocking;
    }



    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
