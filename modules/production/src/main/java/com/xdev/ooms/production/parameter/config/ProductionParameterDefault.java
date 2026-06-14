package com.xdev.ooms.production.parameter.config;

import com.xdev.ooms.sharedkernel.Enum.ParameterType;

public record ProductionParameterDefault(
        String code,
        String category,
        ParameterType type,
        String defaultValue,
        String description
) {
}
