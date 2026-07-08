package com.xdev.ooms.security.companyprofile.dto;

import java.util.List;

public class UpdateTenantModulesRequest {

    private List<String> enabledModules;

    public List<String> getEnabledModules() {
        return enabledModules;
    }

    public void setEnabledModules(List<String> enabledModules) {
        this.enabledModules = enabledModules;
    }
}
