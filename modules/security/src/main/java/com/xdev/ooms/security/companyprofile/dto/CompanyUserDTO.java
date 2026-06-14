package com.xdev.ooms.security.companyprofile.dto;

import com.xdev.ooms.security.user.dto.OSMUserOUTDTO;

public class CompanyUserDTO {
    private String legalName;
    private OSMUserOUTDTO companyUser;

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public OSMUserOUTDTO getCompanyUser() {
        return companyUser;
    }

    public void setCompanyUser(OSMUserOUTDTO companyUser) {
        this.companyUser = companyUser;
    }
}
