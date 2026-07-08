package com.xdev.ooms.health;

import com.xdev.ooms.sharedkernel.settings.dto.PublicObservabilityConfigDto;
import com.xdev.ooms.sharedkernel.settings.service.AppSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/observability-config")
public class PublicObservabilityConfigController {

    private final AppSettingsService appSettingsService;

    public PublicObservabilityConfigController(AppSettingsService appSettingsService) {
        this.appSettingsService = appSettingsService;
    }

    @GetMapping
    public ResponseEntity<PublicObservabilityConfigDto> config() {
        return ResponseEntity.ok(appSettingsService.getPublicObservabilityConfig());
    }
}
