package com.xdev.ooms.sharedkernel.settings.dto;

import java.util.List;

public class FeatureStatusDto {
    private boolean enabled;
    private boolean configured;
    private String provider;
    private List<String> missingKeys;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isConfigured() {
        return configured;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public List<String> getMissingKeys() {
        return missingKeys;
    }

    public void setMissingKeys(List<String> missingKeys) {
        this.missingKeys = missingKeys;
    }
}
