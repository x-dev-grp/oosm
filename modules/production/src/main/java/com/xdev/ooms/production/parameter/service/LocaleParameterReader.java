package com.xdev.ooms.production.parameter.service;

import com.xdev.ooms.production.parameter.entity.Parameter;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.UUID;

@Component
public class LocaleParameterReader {

    public static final String LANGUAGE_CODE = "DEFAULT_LANGUAGE";
    public static final String CURRENCY_CODE = "DEFAULT_CURRENCY";
    public static final String TIMEZONE_CODE = "TIMEZONE";

    private final ParameterService parameterService;

    public LocaleParameterReader(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public String defaultLanguage() {
        String value = readString(LANGUAGE_CODE, "fr");
        String normalized = value == null ? "fr" : value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "en", "fr", "ar" -> normalized;
            default -> "fr";
        };
    }

    public String defaultCurrency() {
        String value = readString(CURRENCY_CODE, "TND");
        return value == null || value.isBlank() ? "TND" : value.trim().toUpperCase(Locale.ROOT);
    }

    public String timezone() {
        String value = readString(TIMEZONE_CODE, "Africa/Tunis");
        return value == null || value.isBlank() ? "Africa/Tunis" : value.trim();
    }

    private String readString(String code, String fallback) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            return fallback;
        }
        try {
            Parameter parameter = parameterService.getByCode(code, tenantId);
            if (parameter == null || parameter.getValue() == null || parameter.getValue().isBlank()) {
                return fallback;
            }
            return parameter.getValue().trim();
        } catch (RuntimeException ex) {
            return fallback;
        }
    }
}
