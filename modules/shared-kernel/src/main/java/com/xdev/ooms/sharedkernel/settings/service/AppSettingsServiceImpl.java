package com.xdev.ooms.sharedkernel.settings.service;

import com.xdev.ooms.sharedkernel.mail.config.MailProvider;
import com.xdev.ooms.sharedkernel.mail.exception.MailDeliveryException;
import com.xdev.ooms.sharedkernel.mail.models.MailRequest;
import com.xdev.ooms.sharedkernel.mail.services.MailService;
import com.xdev.ooms.sharedkernel.notifications.dto.NotificationRequest;
import com.xdev.ooms.sharedkernel.notifications.impl.OneSignalServiceImpl;
import com.xdev.ooms.sharedkernel.settings.definition.AppSettingDefinition;
import com.xdev.ooms.sharedkernel.settings.definition.AppSettingDefinitionRegistry;
import com.xdev.ooms.sharedkernel.settings.dto.AdminSettingAuditDto;
import com.xdev.ooms.sharedkernel.settings.dto.AdminSettingCategoryDto;
import com.xdev.ooms.sharedkernel.settings.dto.AdminSettingDto;
import com.xdev.ooms.sharedkernel.settings.dto.AdminSettingsActor;
import com.xdev.ooms.sharedkernel.settings.dto.AdminSettingsListResponse;
import com.xdev.ooms.sharedkernel.settings.dto.AdminSettingsStatusDto;
import com.xdev.ooms.sharedkernel.settings.dto.FeatureStatusDto;
import com.xdev.ooms.sharedkernel.settings.dto.MailTestRequest;
import com.xdev.ooms.sharedkernel.settings.dto.MailTestResponse;
import com.xdev.ooms.sharedkernel.settings.dto.NotificationTestRequest;
import com.xdev.ooms.sharedkernel.settings.dto.NotificationTestResponse;
import com.xdev.ooms.sharedkernel.settings.dto.RotateSecretRequest;
import com.xdev.ooms.sharedkernel.settings.dto.UpdateSettingRequest;
import com.xdev.ooms.sharedkernel.settings.entity.AppSetting;
import com.xdev.ooms.sharedkernel.settings.entity.AppSettingAudit;
import com.xdev.ooms.sharedkernel.settings.entity.AppSettingMeta;
import com.xdev.ooms.sharedkernel.settings.model.SettingCategory;
import com.xdev.ooms.sharedkernel.settings.model.SettingSource;
import com.xdev.ooms.sharedkernel.settings.model.SettingValueType;
import com.xdev.ooms.sharedkernel.settings.repository.AppSettingAuditRepository;
import com.xdev.ooms.sharedkernel.settings.repository.AppSettingMetaRepository;
import com.xdev.ooms.sharedkernel.settings.repository.AppSettingRepository;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AppSettingsServiceImpl implements AppSettingsService {

    private static final Duration CACHE_TTL = Duration.ofSeconds(30);
    private static final int MASK_MAX_LENGTH = 500;

    private final AppSettingDefinitionRegistry definitionRegistry;
    private final AppSettingRepository settingRepository;
    private final AppSettingAuditRepository auditRepository;
    private final AppSettingMetaRepository metaRepository;
    private final AppSettingEncryptionService encryptionService;
    private final AppSettingValidator validator;
    private final ConfigurableEnvironment environment;
    private final MailService mailService;
    private final OneSignalServiceImpl oneSignalService;
    private final AppSettingsLoggingRefresher loggingRefresher;

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private volatile LocalDateTime lastReloadAt;

    public AppSettingsServiceImpl(
            AppSettingDefinitionRegistry definitionRegistry,
            AppSettingRepository settingRepository,
            AppSettingAuditRepository auditRepository,
            AppSettingMetaRepository metaRepository,
            AppSettingEncryptionService encryptionService,
            AppSettingValidator validator,
            ConfigurableEnvironment environment,
            @Lazy MailService mailService,
            @Lazy OneSignalServiceImpl oneSignalService,
            AppSettingsLoggingRefresher loggingRefresher
    ) {
        this.definitionRegistry = definitionRegistry;
        this.settingRepository = settingRepository;
        this.auditRepository = auditRepository;
        this.metaRepository = metaRepository;
        this.encryptionService = encryptionService;
        this.validator = validator;
        this.environment = environment;
        this.mailService = mailService;
        this.oneSignalService = oneSignalService;
        this.loggingRefresher = loggingRefresher;
    }

    @PostConstruct
    void initDynamicLogLevels() {
        refreshDynamicLogLevels();
    }

    @Override
    public String getString(String key) {
        return resolve(key).rawValue();
    }

    @Override
    public String getString(String key, String fallback) {
        ResolvedSetting resolved = resolve(key);
        return StringUtils.hasText(resolved.rawValue()) ? resolved.rawValue() : fallback;
    }

    @Override
    public boolean getBoolean(String key, boolean fallback) {
        String value = getString(key, null);
        if (!StringUtils.hasText(value)) {
            return fallback;
        }
        return Boolean.parseBoolean(value);
    }

    @Override
    public Optional<String> getSecret(String key) {
        AppSettingDefinition definition = definitionRegistry.find(key).orElse(null);
        if (definition == null || definition.valueType() != SettingValueType.SECRET) {
            return Optional.empty();
        }
        ResolvedSetting resolved = resolve(key);
        return StringUtils.hasText(resolved.rawValue())
                ? Optional.of(resolved.rawValue())
                : Optional.empty();
    }

    @Override
    public AdminSettingsListResponse listForAdmin() {
        Map<SettingCategory, List<AdminSettingDto>> grouped = new EnumMap<>(SettingCategory.class);
        for (AppSettingDefinition definition : definitionRegistry.all()) {
            grouped.computeIfAbsent(definition.category(), ignored -> new ArrayList<>())
                    .add(toAdminDto(definition, resolve(definition.key())));
        }

        List<AdminSettingCategoryDto> categories = new ArrayList<>();
        for (Map.Entry<SettingCategory, List<AdminSettingDto>> entry : grouped.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            AdminSettingCategoryDto categoryDto = new AdminSettingCategoryDto();
            categoryDto.setKey(entry.getKey().name());
            categoryDto.setLabel(categoryLabel(entry.getKey()));
            categoryDto.setSettings(entry.getValue());
            categories.add(categoryDto);
        }

        AdminSettingsListResponse response = new AdminSettingsListResponse();
        response.setCategories(categories);
        return response;
    }

    @Override
    public AdminSettingDto getForAdmin(String key) {
        AppSettingDefinition definition = requireDefinition(key);
        return toAdminDto(definition, resolve(key));
    }

    @Override
    @Transactional
    public AdminSettingDto update(String key, UpdateSettingRequest request, AdminSettingsActor actor) {
        AppSettingDefinition definition = requireDefinition(key);
        String oldMasked = maskForAudit(definition, resolve(key).rawValue(), false);

        try {
            if (definition.sensitive()) {
                throw new IllegalArgumentException("Sensitive settings must be updated via rotate-secret");
            }
            if (!definition.editable()) {
                throw new SecurityException("Setting is not editable");
            }
            if (definition.restartRequired() && !request.isConfirmRestart()) {
                throw new RestartRequiredException("Setting requires service restart confirmation");
            }

            String newValue = request.getValue() != null ? request.getValue().trim() : "";
            validator.validate(definition, newValue);

            AppSetting entity = settingRepository.findBySettingKey(key).orElseGet(() -> createEntity(definition));
            entity.setValue(StringUtils.hasText(newValue) ? newValue : null);
            entity.setEncryptedValue(null);
            entity.setUpdatedBy(actor.getUserId());
            settingRepository.save(entity);

            invalidateCache();
            bumpSettingsVersion();

            writeAudit(key, "UPDATE", oldMasked, maskForAudit(definition, newValue, true),
                    actor, request.getReason(), true, null);

            AdminSettingDto dto = toAdminDto(definition, resolve(key));
            dto.setLastUpdatedBy(actor.getUsername());
            OOSMLogger.logBusinessEvent(this.getClass(), "App setting updated",
                    "key=" + key + " user=" + actor.getUsername() + " source=" + dto.getSource()
                            + " configured=" + dto.isConfigured() + " value=" + maskForAudit(definition, newValue, true));
            applySideEffects(key);
            return dto;
        } catch (RuntimeException ex) {
            OOSMLogger.warn(this.getClass(), "App setting update failed: key={} user={} error={}",
                    key, actor.getUsername(), ex.getMessage());
            writeAudit(key, auditActionForFailure(ex), oldMasked, maskForAudit(definition, request.getValue(), true),
                    actor, request.getReason(), false, ex.getMessage());
            throw ex;
        }
    }

    @Override
    @Transactional
    public AdminSettingDto rotateSecret(String key, RotateSecretRequest request, AdminSettingsActor actor) {
        AppSettingDefinition definition = requireDefinition(key);
        String oldMasked = maskForAudit(definition, resolve(key).rawValue(), false);

        try {
            if (definition.valueType() != SettingValueType.SECRET) {
                throw new IllegalArgumentException("Setting is not a secret");
            }
            if (!definition.editable()) {
                throw new SecurityException("Setting is not editable");
            }

            String newValue = request.getValue() != null ? request.getValue().trim() : "";
            validator.validate(definition, newValue);

            AppSetting entity = settingRepository.findBySettingKey(key).orElseGet(() -> createEntity(definition));
            entity.setValue(null);
            entity.setEncryptedValue(encryptionService.encrypt(key, newValue));
            entity.setEncryptionKeyId("v1");
            entity.setUpdatedBy(actor.getUserId());
            settingRepository.save(entity);

            invalidateCache();
            bumpSettingsVersion();

            writeAudit(key, "ROTATE_SECRET", oldMasked, "<rotated>", actor, request.getReason(), true, null);

            AdminSettingDto dto = toAdminDto(definition, resolve(key));
            dto.setLastUpdatedBy(actor.getUsername());
            OOSMLogger.logBusinessEvent(this.getClass(), "App setting secret rotated",
                    "key=" + key + " user=" + actor.getUsername() + " configured=" + dto.isConfigured());
            return dto;
        } catch (RuntimeException ex) {
            OOSMLogger.warn(this.getClass(), "App setting secret rotation failed: key={} user={} error={}",
                    key, actor.getUsername(), ex.getMessage());
            writeAudit(key, auditActionForFailure(ex), oldMasked, "<rotated>",
                    actor, request.getReason(), false, ex.getMessage());
            throw ex;
        }
    }

    @Override
    public void reload() {
        invalidateCache();
        lastReloadAt = LocalDateTime.now();
        refreshDynamicLogLevels();
        OOSMLogger.info(this.getClass(), "App settings cache invalidated at {}", lastReloadAt);
    }

    @Override
    public AdminSettingsStatusDto getStatus() {
        AdminSettingsStatusDto status = new AdminSettingsStatusDto();
        status.setSettingsVersion(currentSettingsVersion());
        status.setLastReloadAt(lastReloadAt);

        Map<String, Integer> sourceCounts = new LinkedHashMap<>();
        sourceCounts.put(SettingSource.DATABASE.name(), 0);
        sourceCounts.put(SettingSource.ENV.name(), 0);
        sourceCounts.put(SettingSource.DEFAULT.name(), 0);

        List<String> missingRequired = new ArrayList<>();
        for (AppSettingDefinition definition : definitionRegistry.all()) {
            ResolvedSetting resolved = resolve(definition.key());
            sourceCounts.merge(resolved.source().name(), 1, Integer::sum);
            if (definition.requiredForFeature() && !resolved.configured()) {
                missingRequired.add(definition.key());
            }
        }

        status.setSourceCounts(sourceCounts);
        status.setMissingFeatureRequired(missingRequired);

        Map<String, FeatureStatusDto> features = new LinkedHashMap<>();
        features.put("mail", buildMailFeatureStatus());
        features.put("frontend", buildFrontendFeatureStatus());
        features.put("qr", buildQrFeatureStatus());
        features.put("notifications", buildNotificationsFeatureStatus());
        features.put("swagger", buildSwaggerFeatureStatus());
        status.setFeatures(features);

        FeatureStatusDto mail = features.get("mail");
        OOSMLogger.debug(this.getClass(),
                "App settings status computed: version={} mailProvider={} mailEnabled={} mailConfigured={} missing={}",
                status.getSettingsVersion(),
                mail.getProvider(),
                mail.isEnabled(),
                mail.isConfigured(),
                mail.getMissingKeys());
        return status;
    }

    private void applySideEffects(String key) {
        if ("LOG_LEVEL_WEB".equals(key) || "LOG_LEVEL_REST".equals(key)) {
            refreshDynamicLogLevels();
        }
    }

    private void refreshDynamicLogLevels() {
        loggingRefresher.refresh(
                getString("LOG_LEVEL_WEB", "INFO"),
                getString("LOG_LEVEL_REST", "WARN")
        );
    }

    @Override
    public MailTestResponse sendMailTest(MailTestRequest request, AdminSettingsActor actor) {
        MailProvider provider = MailProvider.from(getString("MAIL_PROVIDER", "RESEND"));
        MailTestResponse response = new MailTestResponse();
        response.setProvider(provider.name());

        String recipient = request.getTo() != null ? request.getTo().trim() : "";
        OOSMLogger.info(this.getClass(),
                "Mail test starting: provider={} recipient={} user={} deliveryEnabled={}",
                provider.name(), recipient, actor.getUsername(), mailService.isDeliveryEnabled());
        try {
            if (!StringUtils.hasText(recipient)) {
                throw new IllegalArgumentException("Recipient email is required");
            }
            validateRecipientEmail(recipient);

            if (!mailService.isDeliveryEnabled()) {
                FeatureStatusDto mailStatus = buildMailFeatureStatus();
                String missing = mailStatus.getMissingKeys() != null && !mailStatus.getMissingKeys().isEmpty()
                        ? String.join(", ", mailStatus.getMissingKeys())
                        : "unknown";
                OOSMLogger.warn(this.getClass(),
                        "Mail test blocked, delivery not configured: provider={} missing={} user={}",
                        provider.name(), missing, actor.getUsername());
                throw new IllegalStateException(
                        "Mail delivery is not configured for " + provider.name()
                                + ". Save the required mail fields. Missing: " + missing);
            }

            MailRequest mailRequest = new MailRequest();
            mailRequest.setTo(recipient);
            mailRequest.setSubject("OOSM mail test");
            mailRequest.setBody("This is a test email sent from OOSM administration settings.");
            mailRequest.setHtmlBody("<p>This is a test email sent from <strong>OOSM</strong> administration settings.</p>");

            mailService.sendEmail(mailRequest);

            response.setSuccess(true);
            OOSMLogger.info(this.getClass(),
                    "Mail test sent successfully: provider={} recipient={} user={}",
                    provider.name(), recipient, actor.getUsername());
            writeAudit("MAIL_TEST", "TEST_MAIL", null, recipient, actor, "Mail test", true, null);
            return response;
        } catch (MailDeliveryException ex) {
            response.setSuccess(false);
            response.setError(safeMailTestError(ex));
            OOSMLogger.warn(this.getClass(),
                    "Mail test delivery failed: provider={} recipient={} user={} error={}",
                    provider.name(), recipient, actor.getUsername(), response.getError());
            writeAudit("MAIL_TEST", "TEST_MAIL", null, recipient, actor, "Mail test", false, ex.getMessage());
            return response;
        } catch (RuntimeException ex) {
            response.setSuccess(false);
            response.setError(safeMailTestError(ex));
            OOSMLogger.warn(this.getClass(),
                    "Mail test failed: provider={} recipient={} user={} error={}",
                    provider.name(), recipient, actor.getUsername(), response.getError());
            writeAudit("MAIL_TEST", "TEST_MAIL", null, recipient, actor, "Mail test", false, ex.getMessage());
            return response;
        }
    }

    @Override
    public NotificationTestResponse sendNotificationTest(NotificationTestRequest request, AdminSettingsActor actor) {
        NotificationTestResponse response = new NotificationTestResponse();
        response.setProvider("ONESIGNAL");

        String playerId = request != null && request.getPlayerId() != null ? request.getPlayerId().trim() : "";
        String title = request != null && StringUtils.hasText(request.getTitle())
                ? request.getTitle().trim()
                : "OOSM notification test";
        String message = request != null && StringUtils.hasText(request.getMessage())
                ? request.getMessage().trim()
                : "This is a test push notification from OOSM administration settings.";

        OOSMLogger.info(this.getClass(),
                "Notification test starting: playerId={} user={}",
                maskPlayerId(playerId), actor.getUsername());

        try {
            if (!StringUtils.hasText(playerId)) {
                throw new IllegalArgumentException("OneSignal player ID is required");
            }

            FeatureStatusDto notificationsStatus = buildNotificationsFeatureStatus();
            if (!notificationsStatus.isConfigured()) {
                String missing = notificationsStatus.getMissingKeys() != null && !notificationsStatus.getMissingKeys().isEmpty()
                        ? String.join(", ", notificationsStatus.getMissingKeys())
                        : "unknown";
                throw new IllegalStateException(
                        "OneSignal is not configured. Save the required notification fields. Missing: " + missing);
            }

            NotificationRequest notificationRequest = new NotificationRequest(
                    List.of(playerId),
                    title,
                    message,
                    Map.of("source", "admin-settings-test")
            );
            String result = oneSignalService.sendNotification(notificationRequest);
            response.setResult(result);

            boolean success = "SUCCESS".equals(result);
            response.setSuccess(success);
            if (!success) {
                response.setError(result);
            }

            OOSMLogger.info(this.getClass(),
                    "Notification test finished: success={} result={} user={}",
                    success, result, actor.getUsername());
            writeAudit("NOTIFICATION_TEST", "TEST_NOTIFICATION", null, maskPlayerId(playerId),
                    actor, "Notification test", success, success ? null : result);
            return response;
        } catch (RuntimeException ex) {
            response.setSuccess(false);
            response.setError(ex.getMessage());
            OOSMLogger.warn(this.getClass(),
                    "Notification test failed: playerId={} user={} error={}",
                    maskPlayerId(playerId), actor.getUsername(), ex.getMessage());
            writeAudit("NOTIFICATION_TEST", "TEST_NOTIFICATION", null, maskPlayerId(playerId),
                    actor, "Notification test", false, ex.getMessage());
            return response;
        }
    }

    private static String maskPlayerId(String playerId) {
        if (!StringUtils.hasText(playerId) || playerId.length() < 8) {
            return "***";
        }
        return playerId.substring(0, 4) + "..." + playerId.substring(playerId.length() - 4);
    }

    private static String safeMailTestError(Throwable ex) {
        Throwable root = ex;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        String message = root.getMessage();
        if (!StringUtils.hasText(message)) {
            return "Mail test failed";
        }
        if (message.length() > 500) {
            return message.substring(0, 500);
        }
        return message;
    }

    private void validateRecipientEmail(String recipient) {
        AppSettingDefinition pseudo = new AppSettingDefinition(
                "MAIL_TEST_RECIPIENT",
                SettingCategory.MAIL,
                "Recipient",
                "Mail test recipient",
                SettingValueType.EMAIL,
                false,
                true,
                false,
                false,
                true,
                null,
                null,
                null,
                null
        );
        validator.validate(pseudo, recipient);
    }

    @Override
    public Page<AdminSettingAuditDto> listAudit(String settingKey, Pageable pageable) {
        Page<AppSettingAudit> page = StringUtils.hasText(settingKey)
                ? auditRepository.findBySettingKeyOrderByChangedAtDesc(settingKey, pageable)
                : auditRepository.findAllByOrderByChangedAtDesc(pageable);
        return page.map(this::toAuditDto);
    }

    private ResolvedSetting resolve(String key) {
        CacheEntry cached = cache.get(key);
        if (cached != null && cached.expiresAt().isAfter(Instant.now())) {
            return cached.value();
        }

        AppSettingDefinition definition = definitionRegistry.find(key).orElse(null);
        if (definition == null) {
            ResolvedSetting missing = new ResolvedSetting(null, SettingSource.DEFAULT, false, Optional.empty());
            cache.put(key, new CacheEntry(missing, Instant.now().plus(CACHE_TTL)));
            return missing;
        }

        Optional<AppSetting> dbRow = settingRepository.findBySettingKey(key);
        if (dbRow.isPresent()) {
            AppSetting row = dbRow.get();
            if (definition.sensitive()) {
                if (StringUtils.hasText(row.getEncryptedValue())) {
                    try {
                        String decrypted = encryptionService.decrypt(key, row.getEncryptedValue());
                        ResolvedSetting resolved = new ResolvedSetting(decrypted, SettingSource.DATABASE, true, dbRow);
                        cache.put(key, new CacheEntry(resolved, Instant.now().plus(CACHE_TTL)));
                        return resolved;
                    } catch (RuntimeException ex) {
                        OOSMLogger.warn(this.getClass(),
                                "Failed to decrypt app setting secret from database: key={}", key);
                        ResolvedSetting resolved = new ResolvedSetting(null, SettingSource.DATABASE, false, dbRow);
                        cache.put(key, new CacheEntry(resolved, Instant.now().plus(CACHE_TTL)));
                        return resolved;
                    }
                }
            } else if (StringUtils.hasText(row.getValue())) {
                ResolvedSetting resolved = new ResolvedSetting(row.getValue(), SettingSource.DATABASE, true, dbRow);
                cache.put(key, new CacheEntry(resolved, Instant.now().plus(CACHE_TTL)));
                return resolved;
            }
        }

        String envValue = resolveEnv(definition);
        if (StringUtils.hasText(envValue)) {
            ResolvedSetting resolved = new ResolvedSetting(envValue, SettingSource.ENV, true, dbRow);
            cache.put(key, new CacheEntry(resolved, Instant.now().plus(CACHE_TTL)));
            return resolved;
        }

        Optional<String> defaultValue = definition.defaultValueOptional();
        if (defaultValue.isPresent()) {
            ResolvedSetting resolved = new ResolvedSetting(defaultValue.get(), SettingSource.DEFAULT, true, dbRow);
            cache.put(key, new CacheEntry(resolved, Instant.now().plus(CACHE_TTL)));
            return resolved;
        }

        ResolvedSetting resolved = new ResolvedSetting(null, SettingSource.DEFAULT, false, dbRow);
        cache.put(key, new CacheEntry(resolved, Instant.now().plus(CACHE_TTL)));
        return resolved;
    }

    private String resolveEnv(AppSettingDefinition definition) {
        String envVariable = definition.envVariable();
        if (!StringUtils.hasText(envVariable)) {
            return null;
        }
        String value = System.getenv(envVariable);
        if (!StringUtils.hasText(value)) {
            value = environment.getProperty(envVariable);
        }
        return StringUtils.hasText(value) ? value : null;
    }

    private AdminSettingDto toAdminDto(AppSettingDefinition definition, ResolvedSetting resolved) {
        AdminSettingDto dto = new AdminSettingDto();
        dto.setKey(definition.key());
        dto.setConfigured(resolved.configured());
        dto.setSensitive(definition.sensitive());
        dto.setRestartRequired(definition.restartRequired());
        dto.setReloadable(definition.reloadable());
        dto.setEditable(definition.editable());
        dto.setValueType(definition.valueType().name());
        dto.setSource(resolved.source().name());
        dto.setLabel(definition.label());
        dto.setDescription(definition.description());
        dto.setValue(definition.sensitive() ? null : resolved.rawValue());
        resolved.dbRow().ifPresent(row -> dto.setLastUpdatedAt(row.getUpdatedAt()));
        return dto;
    }

    private AdminSettingAuditDto toAuditDto(AppSettingAudit audit) {
        AdminSettingAuditDto dto = new AdminSettingAuditDto();
        dto.setSettingKey(audit.getSettingKey());
        dto.setAction(audit.getAction());
        dto.setOldValueMasked(audit.getOldValueMasked());
        dto.setNewValueMasked(audit.getNewValueMasked());
        dto.setChangedByUsername(audit.getChangedByUsername());
        dto.setChangedAt(audit.getChangedAt());
        dto.setReason(audit.getReason());
        dto.setSuccess(audit.isSuccess());
        dto.setFailureReason(audit.getFailureReason());
        return dto;
    }

    private FeatureStatusDto buildMailFeatureStatus() {
        MailProvider provider = MailProvider.from(getString("MAIL_PROVIDER", "RESEND"));
        boolean enabled = getBoolean("MAIL_ENABLED", true);
        boolean fromConfigured = StringUtils.hasText(getString("MAIL_FROM_ADDRESS", ""));

        List<String> missing = new ArrayList<>();
        if (!fromConfigured) {
            missing.add("MAIL_FROM_ADDRESS");
        }

        boolean providerConfigured;
        if (provider == MailProvider.SMTP) {
            providerConfigured = isSmtpConfigured(missing);
        } else {
            boolean apiKeyConfigured = getSecret("RESEND_API_KEY").isPresent();
            if (!apiKeyConfigured) {
                missing.add("RESEND_API_KEY");
            }
            providerConfigured = apiKeyConfigured;
        }

        FeatureStatusDto mail = new FeatureStatusDto();
        mail.setProvider(provider.name());
        mail.setEnabled(enabled && providerConfigured && fromConfigured);
        mail.setConfigured(providerConfigured && fromConfigured);
        mail.setMissingKeys(missing);
        return mail;
    }

    private boolean isSmtpConfigured(List<String> missing) {
        boolean configured = true;
        if (!StringUtils.hasText(getString("SMTP_HOST", ""))) {
            missing.add("SMTP_HOST");
            configured = false;
        }
        if (getBoolean("SMTP_AUTH", true)) {
            if (!StringUtils.hasText(getString("SMTP_USERNAME", ""))) {
                missing.add("SMTP_USERNAME");
                configured = false;
            }
            if (!getSecret("SMTP_PASSWORD").isPresent()) {
                missing.add("SMTP_PASSWORD");
                configured = false;
            }
        }
        return configured;
    }

    private FeatureStatusDto buildFrontendFeatureStatus() {
        boolean configured = StringUtils.hasText(getString("FRONTEND_ENTRY_POINT", ""));
        FeatureStatusDto status = new FeatureStatusDto();
        status.setProvider("FRONTEND");
        status.setConfigured(configured);
        status.setEnabled(configured);
        status.setMissingKeys(configured ? List.of() : List.of("FRONTEND_ENTRY_POINT"));
        return status;
    }

    private FeatureStatusDto buildQrFeatureStatus() {
        boolean configured = StringUtils.hasText(getString("QR_BASE_URL", ""));
        FeatureStatusDto status = new FeatureStatusDto();
        status.setProvider("QR");
        status.setConfigured(configured);
        status.setEnabled(configured);
        status.setMissingKeys(configured ? List.of() : List.of("QR_BASE_URL"));
        return status;
    }

    private FeatureStatusDto buildNotificationsFeatureStatus() {
        boolean appIdConfigured = StringUtils.hasText(getString("ONESIGNAL_APP_ID", ""));
        boolean apiKeyConfigured = getSecret("ONESIGNAL_API_KEY").isPresent();
        List<String> missing = new ArrayList<>();
        if (!appIdConfigured) {
            missing.add("ONESIGNAL_APP_ID");
        }
        if (!apiKeyConfigured) {
            missing.add("ONESIGNAL_API_KEY");
        }
        FeatureStatusDto status = new FeatureStatusDto();
        status.setProvider("ONESIGNAL");
        status.setConfigured(appIdConfigured && apiKeyConfigured);
        status.setEnabled(appIdConfigured && apiKeyConfigured);
        status.setMissingKeys(missing);
        return status;
    }

    private FeatureStatusDto buildSwaggerFeatureStatus() {
        boolean configured = getBoolean("SPRINGDOC_ENABLED", false);
        boolean runtimeEnabled = Boolean.parseBoolean(
                environment.getProperty("springdoc.api-docs.enabled", "false"));
        boolean bootstrapLoaded = environment.getPropertySources().contains("appSettingsDatabase");
        String envSpringdoc = environment.getProperty("SPRINGDOC_ENABLED");
        FeatureStatusDto status = new FeatureStatusDto();
        status.setProvider("SPRINGDOC");
        status.setConfigured(configured);
        status.setEnabled(configured && runtimeEnabled);
        status.setMissingKeys(configured ? List.of() : List.of("SPRINGDOC_ENABLED"));
        if (configured && !runtimeEnabled) {
            OOSMLogger.warn(this.getClass(),
                    "SPRINGDOC_ENABLED is true in app settings but springdoc.api-docs.enabled is false at runtime; "
                            + "restart the backend and ensure app_setting bootstrap loaded from the database "
                            + "(bootstrapPropertySourcePresent={}, environment.SPRINGDOC_ENABLED={})",
                    bootstrapLoaded, envSpringdoc);
        }
        return status;
    }

    private AppSetting createEntity(AppSettingDefinition definition) {
        AppSetting entity = new AppSetting();
        entity.setSettingKey(definition.key());
        entity.setValueType(definition.valueType().name());
        entity.setCategory(definition.category().name());
        entity.setLabel(definition.label());
        entity.setDescription(definition.description());
        entity.setSensitive(definition.sensitive());
        entity.setEditable(definition.editable());
        entity.setRestartRequired(definition.restartRequired());
        entity.setRequiredForFeature(definition.requiredForFeature());
        return entity;
    }

    private void writeAudit(
            String settingKey,
            String action,
            String oldMasked,
            String newMasked,
            AdminSettingsActor actor,
            String reason,
            boolean success,
            String failureReason
    ) {
        AppSettingAudit audit = new AppSettingAudit();
        audit.setSettingKey(settingKey);
        audit.setAction(action);
        audit.setOldValueMasked(oldMasked);
        audit.setNewValueMasked(truncate(newMasked));
        audit.setChangedBy(actor.getUserId());
        audit.setChangedByUsername(actor.getUsername());
        audit.setIpAddress(actor.getIpAddress());
        audit.setUserAgent(actor.getUserAgent());
        audit.setReason(reason);
        audit.setSuccess(success);
        audit.setFailureReason(failureReason);
        auditRepository.save(audit);
    }

    private String maskForAudit(AppSettingDefinition definition, String value, boolean isNewSecret) {
        if (definition.sensitive() || definition.valueType() == SettingValueType.SECRET) {
            if (!StringUtils.hasText(value)) {
                return "<empty>";
            }
            return isNewSecret ? "<rotated>" : "<configured>";
        }
        return truncate(value);
    }

    private static String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() > MASK_MAX_LENGTH ? value.substring(0, MASK_MAX_LENGTH) : value;
    }

    private static String auditActionForFailure(RuntimeException ex) {
        if (ex instanceof RestartRequiredException) {
            return "UPDATE";
        }
        if (ex instanceof IllegalArgumentException) {
            return "VALIDATION_FAILED";
        }
        if (ex instanceof SecurityException) {
            return "PERMISSION_DENIED";
        }
        return "UPDATE";
    }

    private void invalidateCache() {
        cache.clear();
    }

    private void bumpSettingsVersion() {
        AppSettingMeta meta = metaRepository.findById("global").orElseGet(() -> {
            AppSettingMeta created = new AppSettingMeta();
            created.setId("global");
            created.setSettingsVersion(0);
            created.setUpdatedAt(LocalDateTime.now());
            return created;
        });
        meta.setSettingsVersion(meta.getSettingsVersion() + 1);
        meta.setUpdatedAt(LocalDateTime.now());
        metaRepository.save(meta);
    }

    private long currentSettingsVersion() {
        return metaRepository.findById("global")
                .map(AppSettingMeta::getSettingsVersion)
                .orElse(0L);
    }

    private AppSettingDefinition requireDefinition(String key) {
        return definitionRegistry.find(key)
                .orElseThrow(() -> new SettingNotFoundException("Unknown setting key: " + key));
    }

    private static String categoryLabel(SettingCategory category) {
        return switch (category) {
            case MAIL -> "Mail";
            case FRONTEND -> "Frontend";
            case SECURITY -> "Security";
            case QR -> "QR";
            case NOTIFICATIONS -> "Notifications";
            case DIAGNOSTICS -> "Diagnostics";
            case INTEGRATIONS -> "Integrations";
        };
    }

    private record ResolvedSetting(String rawValue, SettingSource source, boolean configured, Optional<AppSetting> dbRow) {}

    private record CacheEntry(ResolvedSetting value, Instant expiresAt) {}

    public static class SettingNotFoundException extends RuntimeException {
        public SettingNotFoundException(String message) {
            super(message);
        }
    }

    public static class RestartRequiredException extends RuntimeException {
        public RestartRequiredException(String message) {
            super(message);
        }
    }
}
