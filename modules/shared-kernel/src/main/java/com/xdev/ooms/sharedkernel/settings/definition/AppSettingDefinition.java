package com.xdev.ooms.sharedkernel.settings.definition;

import com.xdev.ooms.sharedkernel.settings.model.SettingCategory;
import com.xdev.ooms.sharedkernel.settings.model.SettingValueType;

import java.util.List;
import java.util.Optional;

public record AppSettingDefinition(
        String key,
        SettingCategory category,
        String label,
        String description,
        SettingValueType valueType,
        boolean sensitive,
        boolean editable,
        boolean requiredForFeature,
        boolean restartRequired,
        boolean reloadable,
        String defaultValue,
        String envVariable,
        List<String> allowedValues,
        String validationPrefix
) {
    public Optional<String> defaultValueOptional() {
        return defaultValue == null || defaultValue.isBlank()
                ? Optional.empty()
                : Optional.of(defaultValue);
    }
}
