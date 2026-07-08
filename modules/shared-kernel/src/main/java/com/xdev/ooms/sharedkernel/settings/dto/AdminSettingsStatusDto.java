package com.xdev.ooms.sharedkernel.settings.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AdminSettingsStatusDto {
    private long settingsVersion;
    private LocalDateTime lastReloadAt;
    private Map<String, Integer> sourceCounts;
    private Map<String, FeatureStatusDto> features;
    private List<String> missingFeatureRequired;

    public long getSettingsVersion() {
        return settingsVersion;
    }

    public void setSettingsVersion(long settingsVersion) {
        this.settingsVersion = settingsVersion;
    }

    public LocalDateTime getLastReloadAt() {
        return lastReloadAt;
    }

    public void setLastReloadAt(LocalDateTime lastReloadAt) {
        this.lastReloadAt = lastReloadAt;
    }

    public Map<String, Integer> getSourceCounts() {
        return sourceCounts;
    }

    public void setSourceCounts(Map<String, Integer> sourceCounts) {
        this.sourceCounts = sourceCounts;
    }

    public Map<String, FeatureStatusDto> getFeatures() {
        return features;
    }

    public void setFeatures(Map<String, FeatureStatusDto> features) {
        this.features = features;
    }

    public List<String> getMissingFeatureRequired() {
        return missingFeatureRequired;
    }

    public void setMissingFeatureRequired(List<String> missingFeatureRequired) {
        this.missingFeatureRequired = missingFeatureRequired;
    }
}
