package com.xdev.ooms.production.parameter.service;

import com.xdev.ooms.production.parameter.entity.Parameter;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Fail-open reader for boolean system parameters.
 */
@Component
public class BooleanParameterReader {

    private final ParameterService parameterService;

    public BooleanParameterReader(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public boolean isEnabled(String code, boolean defaultValue) {
        return isEnabled(code, TenantContext.getCurrentTenant(), defaultValue);
    }

    public boolean isEnabled(String code, UUID tenantId, boolean defaultValue) {
        if (tenantId == null || code == null || code.isBlank()) {
            return defaultValue;
        }
        try {
            Parameter parameter = parameterService.getByCode(code, tenantId);
            if (parameter == null || parameter.getValue() == null || parameter.getValue().isBlank()) {
                return defaultValue;
            }
            return Boolean.parseBoolean(parameter.getValue().trim());
        } catch (RuntimeException ex) {
            return defaultValue;
        }
    }
}
