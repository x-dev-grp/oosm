package com.xdev.ooms.production.parameter.service;

import com.xdev.ooms.production.parameter.entity.Parameter;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.UUID;

@Component
public class PrintParameterReader {

    public static final String PAPER_SIZE_CODE = "PRINT_PAPER_SIZE";
    public static final String SHOW_QR_CODE = "PRINT_SHOW_QR";
    public static final String COPIES_CODE = "PRINT_COPIES";

    private final ParameterService parameterService;
    private final BooleanParameterReader booleanParameterReader;

    public PrintParameterReader(ParameterService parameterService, BooleanParameterReader booleanParameterReader) {
        this.parameterService = parameterService;
        this.booleanParameterReader = booleanParameterReader;
    }

    public String paperSize() {
        String value = readString(PAPER_SIZE_CODE, "A4");
        String normalized = value == null ? "A4" : value.trim().toUpperCase(Locale.ROOT);
        return "TICKET".equals(normalized) ? "TICKET" : "A4";
    }

    public boolean showQr() {
        return booleanParameterReader.isEnabled(SHOW_QR_CODE, true);
    }

    public int copies() {
        String raw = readString(COPIES_CODE, "1");
        try {
            int parsed = Integer.parseInt(raw.trim());
            return Math.max(1, Math.min(parsed, 10));
        } catch (Exception ex) {
            return 1;
        }
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
