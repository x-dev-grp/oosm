package com.xdev.ooms.sharedkernel.settings.service;

import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AppSettingsLoggingRefresher {

    public void refresh(String webLevel, String restLevel) {
        apply("org.springframework.web", webLevel, "INFO");
        apply("org.springframework.web.client.RestTemplate", restLevel, "WARN");
    }

    private void apply(String loggerName, String rawLevel, String fallback) {
        String levelValue = StringUtils.hasText(rawLevel) ? rawLevel.trim() : fallback;
        try {
            LogLevel level = LogLevel.valueOf(levelValue.toUpperCase());
            LoggingSystem.get(getClass().getClassLoader()).setLogLevel(loggerName, level);
            OOSMLogger.info(this.getClass(), "Applied log level {}={}", loggerName, level);
        } catch (IllegalArgumentException ex) {
            OOSMLogger.warn(this.getClass(), "Ignored invalid log level for {}: {}", loggerName, levelValue);
        } catch (IllegalStateException ex) {
            OOSMLogger.warn(this.getClass(), "Logging system not ready; skipped log level for {}: {}", loggerName, levelValue);
        }
    }
}
