package com.xdev.ooms.sharedkernel.models;

public enum OOSMModule {
    HR(0), RECEPTION(1), PRODUCTION(2), FINANCE(3), HABILITATION(4), INVENTAIR(5), CONDITIONING(6);


    private final int value;

    OOSMModule(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}

