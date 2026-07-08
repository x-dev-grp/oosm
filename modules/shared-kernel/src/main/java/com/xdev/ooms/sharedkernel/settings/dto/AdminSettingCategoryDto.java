package com.xdev.ooms.sharedkernel.settings.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AdminSettingCategoryDto {
    private String key;
    private String label;
    private List<AdminSettingDto> settings;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<AdminSettingDto> getSettings() {
        return settings;
    }

    public void setSettings(List<AdminSettingDto> settings) {
        this.settings = settings;
    }
}
