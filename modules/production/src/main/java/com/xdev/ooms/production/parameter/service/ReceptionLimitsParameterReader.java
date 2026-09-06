package com.xdev.ooms.production.parameter.service;

import com.xdev.ooms.production.parameter.entity.Parameter;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Component
public class ReceptionLimitsParameterReader {

    public static final String OPEN_TIME_CODE = "RECEPTION_OPEN_TIME";
    public static final String CLOSE_TIME_CODE = "RECEPTION_CLOSE_TIME";
    public static final String MAX_DAILY_TONNAGE_CODE = "MAX_DAILY_TONNAGE_KG";
    public static final String DEFAULT_VARIETY_CODE = "DEFAULT_OLIVE_VARIETY";
    public static final String DEFAULT_METHOD_CODE = "DEFAULT_PRODUCTION_METHOD";
    public static final String ENABLE_MILL_PLANNING_CODE = "ENABLE_MILL_PLANNING";

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("H:mm");

    private final ParameterService parameterService;

    public ReceptionLimitsParameterReader(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public LocalTime openTime() {
        return readTime(OPEN_TIME_CODE, LocalTime.of(7, 0));
    }

    public LocalTime closeTime() {
        return readTime(CLOSE_TIME_CODE, LocalTime.of(18, 0));
    }

    public boolean isWithinReceptionHours(LocalTime now) {
        LocalTime current = now != null ? now : LocalTime.now();
        LocalTime open = openTime();
        LocalTime close = closeTime();
        if (open.equals(close)) {
            return true;
        }
        if (open.isBefore(close)) {
            return !current.isBefore(open) && !current.isAfter(close);
        }
        // Overnight window (e.g. 22:00 -> 06:00)
        return !current.isBefore(open) || !current.isAfter(close);
    }

    /** 0 means disabled. */
    public double maxDailyTonnageKg() {
        return readDouble(MAX_DAILY_TONNAGE_CODE, 0d);
    }

    public String defaultOliveVariety() {
        return readString(DEFAULT_VARIETY_CODE, "");
    }

    public String defaultProductionMethod() {
        return readString(DEFAULT_METHOD_CODE, "");
    }

    /** Default true — keeps existing kanban mill planning behavior. */
    public boolean isMillPlanningEnabled() {
        return readBoolean(ENABLE_MILL_PLANNING_CODE, true);
    }

    private boolean readBoolean(String code, boolean fallback) {
        String raw = readString(code, null);
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        String normalized = raw.trim().toLowerCase();
        if (List.of("true", "1", "yes").contains(normalized)) {
            return true;
        }
        if (List.of("false", "0", "no").contains(normalized)) {
            return false;
        }
        return fallback;
    }

    private LocalTime readTime(String code, LocalTime fallback) {
        String raw = readString(code, null);
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return LocalTime.parse(raw.trim(), TIME_FMT);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private double readDouble(String code, double fallback) {
        String raw = readString(code, null);
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Double.parseDouble(raw.replace(',', '.').trim());
        } catch (Exception ex) {
            return fallback;
        }
    }

    private String readString(String code, String fallback) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            return fallback;
        }
        try {
            Parameter parameter = parameterService.getByCode(code, tenantId);
            if (parameter == null || parameter.getValue() == null) {
                return fallback;
            }
            String value = parameter.getValue().trim();
            return value.isEmpty() ? fallback : value;
        } catch (RuntimeException ex) {
            return fallback;
        }
    }
}
