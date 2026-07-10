package com.xdev.ooms.sharedkernel.settings.definition;

import com.xdev.ooms.sharedkernel.settings.model.SettingCategory;
import com.xdev.ooms.sharedkernel.settings.model.SettingValueType;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class AppSettingDefinitionRegistry {

    private static final List<String> LOG_LEVELS = List.of("TRACE", "DEBUG", "INFO", "WARN", "ERROR", "OFF");
    private static final List<String> HEALTH_DETAIL_LEVELS = List.of("never", "when_authorized", "always");

    private final Map<String, AppSettingDefinition> definitions;

    public AppSettingDefinitionRegistry() {
        this.definitions = new LinkedHashMap<>();
        registerMailSettings();
        registerFrontendSettings();
        registerQrSettings();
        registerNotificationSettings();
        registerDiagnosticSettings();
    }

    private void registerMailSettings() {
        register(setting(SettingCategory.MAIL, "MAIL_ENABLED", "Mail enabled", "Enable outbound email delivery.",
                SettingValueType.BOOLEAN, false, false, "true", "MAIL_ENABLED", null, null));
        register(settingEnum(SettingCategory.MAIL, "MAIL_PROVIDER", "Mail provider",
                "Active mail delivery provider (Resend API or SMTP).",
                false, false, "RESEND", "MAIL_PROVIDER", List.of("RESEND", "SMTP")));
        register(setting(SettingCategory.MAIL, "MAIL_FROM_ADDRESS", "From address", "Sender email address.",
                SettingValueType.EMAIL, true, false, null, "MAIL_FROM_ADDRESS", null, null));
        register(setting(SettingCategory.MAIL, "MAIL_FROM_NAME", "From name", "Display name for outbound emails.",
                SettingValueType.STRING, false, false, "OOSM", "MAIL_FROM_NAME", null, null));
        register(setting(SettingCategory.MAIL, "MAIL_SUPPORT_EMAIL", "Support email", "Reply-to address for outbound emails.",
                SettingValueType.EMAIL, false, false, null, "MAIL_SUPPORT_EMAIL", null, null));
        register(setting(SettingCategory.MAIL, "MAIL_DEBUG", "Mail debug", "Enable verbose mail logging.",
                SettingValueType.BOOLEAN, false, false, "false", "MAIL_DEBUG", null, null));
        register(secret(SettingCategory.MAIL, "RESEND_API_KEY", "Resend API key",
                "Write-only Resend API key for transactional email.", "RESEND_API_KEY", "re_"));
        register(setting(SettingCategory.MAIL, "SMTP_HOST", "SMTP host", "SMTP server hostname.",
                SettingValueType.STRING, false, false, null, "SMTP_HOST", null, null));
        register(setting(SettingCategory.MAIL, "SMTP_PORT", "SMTP port", "SMTP server port.",
                SettingValueType.INTEGER, false, false, "587", "SMTP_PORT", null, null));
        register(setting(SettingCategory.MAIL, "SMTP_USERNAME", "SMTP username", "SMTP authentication username.",
                SettingValueType.STRING, false, false, null, "SMTP_USERNAME", null, null));
        register(secret(SettingCategory.MAIL, "SMTP_PASSWORD", "SMTP password",
                "Write-only SMTP password.", "SMTP_PASSWORD", null));
        register(setting(SettingCategory.MAIL, "SMTP_AUTH", "SMTP authentication", "Enable SMTP authentication.",
                SettingValueType.BOOLEAN, false, false, "true", "SMTP_AUTH", null, null));
        register(setting(SettingCategory.MAIL, "SMTP_STARTTLS", "SMTP STARTTLS", "Enable STARTTLS for SMTP connections.",
                SettingValueType.BOOLEAN, false, false, "true", "SMTP_STARTTLS", null, null));
    }

    private void registerFrontendSettings() {
        register(setting(SettingCategory.FRONTEND, "FRONTEND_ENTRY_POINT", "Frontend URL",
                "Public URL of the Angular app (used in emails, health checks, CORS).",
                SettingValueType.URL, false, false, "http://localhost:4200", "FRONTEND_ENTRY_POINT", null, null));
        register(setting(SettingCategory.FRONTEND, "APP_CORS_ALLOWED_ORIGIN_PATTERNS", "CORS allowed origins",
                "Comma-separated origin patterns allowed to call the API. Wildcard * is rejected.",
                SettingValueType.CSV, false, false, null, "APP_CORS_ALLOWED_ORIGIN_PATTERNS", null, null));
    }

    private void registerQrSettings() {
        register(setting(SettingCategory.QR, "QR_BASE_URL", "QR base URL",
                "Base URL embedded in generated QR codes.",
                SettingValueType.URL, false, false, "https://x-dev.pro/q/v1", "QR_BASE_URL", null, null));
    }

    private void registerNotificationSettings() {
        register(setting(SettingCategory.NOTIFICATIONS, "ONESIGNAL_APP_ID", "OneSignal app ID",
                "OneSignal application identifier.",
                SettingValueType.STRING, false, false, null, "ONESIGNAL_APP_ID", null, null));
        register(secret(SettingCategory.NOTIFICATIONS, "ONESIGNAL_API_KEY", "OneSignal API key",
                "Write-only OneSignal REST API key.", "ONESIGNAL_API_KEY", null));
        register(setting(SettingCategory.NOTIFICATIONS, "ONESIGNAL_ENDPOINT", "OneSignal endpoint",
                "OneSignal notifications API endpoint.",
                SettingValueType.URL, false, false, "https://api.onesignal.com/notifications",
                "ONESIGNAL_ENDPOINT", null, null));
    }

    private void registerDiagnosticSettings() {
        register(settingEnum(SettingCategory.DIAGNOSTICS, "LOG_LEVEL_WEB", "Web log level",
                "Log level for org.springframework.web (applied on save/reload).",
                false, false, "INFO", "LOG_LEVEL_WEB", LOG_LEVELS));
        register(settingEnum(SettingCategory.DIAGNOSTICS, "LOG_LEVEL_REST", "REST client log level",
                "Log level for RestTemplate (applied on save/reload).",
                false, false, "WARN", "LOG_LEVEL_REST", LOG_LEVELS));
        register(setting(SettingCategory.DIAGNOSTICS, "SEARCH_DEBUG_ON_STARTUP", "Search debug on startup",
                "Enable SearchDebugger on application startup. Requires restart.",
                SettingValueType.BOOLEAN, false, true, "false", "SEARCH_DEBUG_ON_STARTUP", null, null));
        register(settingEnum(SettingCategory.DIAGNOSTICS, "HEALTH_SHOW_DETAILS", "Health show details",
                "Actuator health detail level. Requires restart.",
                false, true, "never", "HEALTH_SHOW_DETAILS", HEALTH_DETAIL_LEVELS));
        register(setting(SettingCategory.DIAGNOSTICS, "SPRINGDOC_ENABLED", "OpenAPI / Swagger enabled",
                "Expose /swagger-ui and /v3/api-docs. Requires restart.",
                SettingValueType.BOOLEAN, false, true, "false", "SPRINGDOC_ENABLED", null, null));
    }

    private static AppSettingDefinition settingEnum(
            SettingCategory category,
            String key,
            String label,
            String description,
            boolean requiredForFeature,
            boolean restartRequired,
            String defaultValue,
            String envVariable,
            List<String> allowedValues
    ) {
        return new AppSettingDefinition(
                key, category, label, description, SettingValueType.ENUM,
                false, true, requiredForFeature, restartRequired, true,
                defaultValue, envVariable, allowedValues, null
        );
    }

    private static AppSettingDefinition setting(
            SettingCategory category,
            String key,
            String label,
            String description,
            SettingValueType type,
            boolean requiredForFeature,
            boolean restartRequired,
            String defaultValue,
            String envVariable,
            List<String> allowedValues,
            String validationPrefix
    ) {
        return new AppSettingDefinition(
                key, category, label, description, type,
                false, true, requiredForFeature, restartRequired, true,
                defaultValue, envVariable, allowedValues, validationPrefix
        );
    }

    private static AppSettingDefinition secret(
            SettingCategory category,
            String key,
            String label,
            String description,
            String envVariable,
            String validationPrefix
    ) {
        return new AppSettingDefinition(
                key, category, label, description, SettingValueType.SECRET,
                true, true, false, false, true,
                null, envVariable, null, validationPrefix
        );
    }

    private void register(AppSettingDefinition definition) {
        definitions.put(definition.key(), definition);
    }

    public Optional<AppSettingDefinition> find(String key) {
        return Optional.ofNullable(definitions.get(key));
    }

    public AppSettingDefinition require(String key) {
        return find(key).orElseThrow(() -> new IllegalArgumentException("Unknown setting key: " + key));
    }

    public Collection<AppSettingDefinition> all() {
        return definitions.values();
    }
}
