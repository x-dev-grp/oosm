package com.xdev.ooms.sharedkernel.communicator.models.shared;

public class ConfirmationCodeDTO   {
    private String code;
    private ConfirmationCodeType confirmationCodeType;
    private OOSMUserDTO user;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ConfirmationCodeType getConfirmationCodeType() {
        return confirmationCodeType;
    }

    public void setConfirmationCodeType(ConfirmationCodeType confirmationCodeType) {
        this.confirmationCodeType = confirmationCodeType;
    }

    public OOSMUserDTO getUser() {
        return user;
    }

    public void setUser(OOSMUserDTO user) {
        this.user = user;
    }
}
