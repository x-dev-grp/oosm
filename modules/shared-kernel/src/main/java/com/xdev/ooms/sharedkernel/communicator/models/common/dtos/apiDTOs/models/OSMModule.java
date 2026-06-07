package com.xdev.ooms.sharedkernel.communicator.models.common.dtos.apiDTOs.models;

public enum OSMModule {
    HR(0), RECEPTION(1), PRODUCTION(2), FINANCE(3), HABILITATION(4), INVENTAIR(5), CONDITIONING(6);


    private final int value;

    OSMModule(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}

