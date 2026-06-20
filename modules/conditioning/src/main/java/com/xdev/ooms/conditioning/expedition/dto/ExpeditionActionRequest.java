package com.xdev.ooms.conditioning.expedition.dto;

public class ExpeditionActionRequest {
    private String comment;
    private String location;

    public String getComment() {
        return comment;
    }

    public String getLocation() {
        return location;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
