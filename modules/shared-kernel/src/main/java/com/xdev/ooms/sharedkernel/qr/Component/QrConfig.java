package com.xdev.ooms.sharedkernel.qr.Component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class QrConfig {

    @Value("${qr.base-url:https://x-dev.pro/q/v1}")
    private String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }
}