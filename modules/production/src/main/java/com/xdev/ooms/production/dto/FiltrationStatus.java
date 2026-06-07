package com.xdev.ooms.production.dto;

import lombok.Getter;

@Getter
public enum FiltrationStatus {

    CREATED("Created"), IN_PROGRESS("In Progress"), COMPLETED("Completed"), CANCELLED("Cancelled");

    private final String name;

    FiltrationStatus(String name) {
        this.name = name;
    }
}