package com.xdev.ooms.sharedkernel.settings.service;

import com.xdev.ooms.sharedkernel.settings.dto.AdminSettingAuditDto;
import com.xdev.ooms.sharedkernel.settings.dto.AdminSettingDto;
import com.xdev.ooms.sharedkernel.settings.dto.AdminSettingsActor;
import com.xdev.ooms.sharedkernel.settings.dto.AdminSettingsListResponse;
import com.xdev.ooms.sharedkernel.settings.dto.AdminSettingsStatusDto;
import com.xdev.ooms.sharedkernel.settings.dto.MailTestRequest;
import com.xdev.ooms.sharedkernel.settings.dto.MailTestResponse;
import com.xdev.ooms.sharedkernel.settings.dto.NotificationTestRequest;
import com.xdev.ooms.sharedkernel.settings.dto.NotificationTestResponse;
import com.xdev.ooms.sharedkernel.settings.dto.RotateSecretRequest;
import com.xdev.ooms.sharedkernel.settings.dto.UpdateSettingRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface AppSettingsService {
    String getString(String key);

    String getString(String key, String fallback);

    boolean getBoolean(String key, boolean fallback);

    Optional<String> getSecret(String key);

    AdminSettingsListResponse listForAdmin();

    AdminSettingDto getForAdmin(String key);

    AdminSettingDto update(String key, UpdateSettingRequest request, AdminSettingsActor actor);

    AdminSettingDto rotateSecret(String key, RotateSecretRequest request, AdminSettingsActor actor);

    void reload();

    AdminSettingsStatusDto getStatus();

    MailTestResponse sendMailTest(MailTestRequest request, AdminSettingsActor actor);

    NotificationTestResponse sendNotificationTest(NotificationTestRequest request, AdminSettingsActor actor);

    Page<AdminSettingAuditDto> listAudit(String settingKey, Pageable pageable);
}
