package com.xdev.ooms.sharedkernel.qr.Component;

import com.xdev.ooms.sharedkernel.settings.service.AppSettingsService;
import org.springframework.stereotype.Component;

@Component
public class QrConfig {

    private final AppSettingsService appSettingsService;

    public QrConfig(AppSettingsService appSettingsService) {
        this.appSettingsService = appSettingsService;
    }

    public String getBaseUrl() {
        return appSettingsService.getString("QR_BASE_URL", "https://x-dev.pro/q/v1");
    }
}
