package com.xdev.ooms.sharedkernel.settings.dto;

import java.util.List;

public class AdminSettingsListResponse {
    private List<AdminSettingCategoryDto> categories;

    public List<AdminSettingCategoryDto> getCategories() {
        return categories;
    }

    public void setCategories(List<AdminSettingCategoryDto> categories) {
        this.categories = categories;
    }
}
