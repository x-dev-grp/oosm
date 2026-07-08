package com.xdev.ooms.security.companyprofile.dto;

import com.xdev.ooms.security.user.dto.OOSMUserOUTDTO;

import java.util.List;

public class CompanyUserDTO {
    private String legalName;
    private OOSMUserOUTDTO companyUser;
    private List<String> enabledModules;

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public OOSMUserOUTDTO getCompanyUser() {
        return companyUser;
    }

    public void setCompanyUser(OOSMUserOUTDTO companyUser) {
        this.companyUser = companyUser;
    }

    public List<String> getEnabledModules() {
        return enabledModules;
    }

    public void setEnabledModules(List<String> enabledModules) {
        this.enabledModules = enabledModules;
    }
}
