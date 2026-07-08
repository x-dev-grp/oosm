package com.xdev.ooms.sharedkernel.settings.service;

import com.xdev.ooms.sharedkernel.settings.definition.AppSettingDefinition;
import com.xdev.ooms.sharedkernel.settings.model.SettingValueType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class AppSettingValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public void validate(AppSettingDefinition definition, String value) {
        if (!StringUtils.hasText(value)) {
            if (definition.requiredForFeature()) {
                throw new IllegalArgumentException(definition.key() + " is required");
            }
            return;
        }

        switch (definition.valueType()) {
            case BOOLEAN -> validateBoolean(definition.key(), value);
            case EMAIL -> validateEmail(definition.key(), value);
            case URL -> validateUrl(definition.key(), value);
            case INTEGER -> validateInteger(definition.key(), value);
            case ENUM -> validateEnum(definition, value);
            case SECRET -> validateSecret(definition, value);
            case STRING, CSV, JSON -> validateString(definition, value);
        }
    }

    private void validateString(AppSettingDefinition definition, String value) {
        if ("SMTP_HOST".equals(definition.key()) && "smtp.google.com".equalsIgnoreCase(value.trim())) {
            throw new IllegalArgumentException(
                    "SMTP_HOST must be smtp.gmail.com for Gmail (smtp.google.com is not a valid SMTP server)");
        }
        if ("APP_CORS_ALLOWED_ORIGIN_PATTERNS".equals(definition.key())) {
            for (String pattern : value.split(",")) {
                if ("*".equals(pattern.trim())) {
                    throw new IllegalArgumentException("APP_CORS_ALLOWED_ORIGIN_PATTERNS must not contain wildcard *");
                }
            }
        }
    }

    private void validateBoolean(String key, String value) {
        if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
            throw new IllegalArgumentException(key + " must be true or false");
        }
    }

    private void validateEmail(String key, String value) {
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(key + " must be a valid email address");
        }
    }

    private void validateUrl(String key, String value) {
        try {
            URI uri = URI.create(value);
            if (!StringUtils.hasText(uri.getScheme()) || !StringUtils.hasText(uri.getHost())) {
                throw new IllegalArgumentException(key + " must be a valid URL");
            }
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(key + " must be a valid URL");
        }
    }

    private void validateInteger(String key, String value) {
        try {
            Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(key + " must be an integer");
        }
    }

    private void validateEnum(AppSettingDefinition definition, String value) {
        List<String> allowed = definition.allowedValues();
        if (allowed == null || allowed.isEmpty()) {
            return;
        }
        if (allowed.stream().noneMatch(candidate -> candidate.equalsIgnoreCase(value))) {
            throw new IllegalArgumentException(definition.key() + " has an invalid value");
        }
    }

    private void validateSecret(AppSettingDefinition definition, String value) {
        String prefix = definition.validationPrefix();
        if (StringUtils.hasText(prefix) && !value.startsWith(prefix)) {
            throw new IllegalArgumentException(definition.key() + " has an invalid format");
        }
    }
}
