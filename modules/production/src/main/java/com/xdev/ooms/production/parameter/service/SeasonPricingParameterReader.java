package com.xdev.ooms.production.parameter.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xdev.ooms.production.parameter.entity.Parameter;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Resolves trituration price from SEASON_PRICING_RULES, falling back to PRIX_TRITURATION_KG.
 */
@Component
public class SeasonPricingParameterReader {

    public static final String SEASON_RULES_CODE = "SEASON_PRICING_RULES";
    public static final String FLAT_PRICE_CODE = "PRIX_TRITURATION_KG";
    private static final double DEFAULT_PRICE = 0.170d;

    private final ParameterService parameterService;
    private final ObjectMapper objectMapper;

    public SeasonPricingParameterReader(ParameterService parameterService, ObjectMapper objectMapper) {
        this.parameterService = parameterService;
        this.objectMapper = objectMapper;
    }

    public double resolvePricePerKg(LocalDate onDate, String varietyName) {
        return resolvePricePerKg(TenantContext.getCurrentTenant(), onDate, varietyName);
    }

    public double resolvePricePerKg(UUID tenantId, LocalDate onDate, String varietyName) {
        LocalDate date = onDate != null ? onDate : LocalDate.now();
        List<SeasonPricingRule> rules = readRules(tenantId);
        String variety = normalize(varietyName);

        for (SeasonPricingRule rule : rules) {
            if (!rule.matchesDate(date)) {
                continue;
            }
            if (!rule.matchesVariety(variety)) {
                continue;
            }
            if (rule.pricePerKg() != null && rule.pricePerKg() >= 0) {
                return rule.pricePerKg();
            }
        }
        return readFlatPrice(tenantId);
    }

    private double readFlatPrice(UUID tenantId) {
        if (tenantId == null) {
            return DEFAULT_PRICE;
        }
        try {
            Parameter parameter = parameterService.getByCode(FLAT_PRICE_CODE, tenantId);
            if (parameter != null && parameter.getValue() != null && !parameter.getValue().isBlank()) {
                double parsed = Double.parseDouble(parameter.getValue().replace(',', '.').trim());
                return parsed >= 0 ? parsed : DEFAULT_PRICE;
            }
        } catch (RuntimeException ignored) {
            // fall through
        }
        return DEFAULT_PRICE;
    }

    private List<SeasonPricingRule> readRules(UUID tenantId) {
        if (tenantId == null) {
            return List.of();
        }
        try {
            Parameter parameter = parameterService.getByCode(SEASON_RULES_CODE, tenantId);
            if (parameter == null || parameter.getValue() == null || parameter.getValue().isBlank()) {
                return List.of();
            }
            List<SeasonPricingRule> parsed = objectMapper.readValue(
                    parameter.getValue(),
                    new TypeReference<List<SeasonPricingRule>>() {
                    });
            return parsed != null ? parsed : List.of();
        } catch (Exception ex) {
            return List.of();
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public record SeasonPricingRule(
            String from,
            String to,
            String variety,
            Double pricePerKg
    ) {
        boolean matchesDate(LocalDate date) {
            LocalDate start = parseDate(from);
            LocalDate end = parseDate(to);
            if (start != null && date.isBefore(start)) {
                return false;
            }
            if (end != null && date.isAfter(end)) {
                return false;
            }
            return true;
        }

        boolean matchesVariety(String varietyNormalized) {
            String ruleVariety = normalize(variety);
            if (ruleVariety.isBlank() || "*".equals(ruleVariety)) {
                return true;
            }
            return ruleVariety.equals(varietyNormalized);
        }

        private static LocalDate parseDate(String raw) {
            if (raw == null || raw.isBlank()) {
                return null;
            }
            try {
                return LocalDate.parse(raw.trim());
            } catch (Exception ex) {
                return null;
            }
        }

        private static String normalize(String value) {
            return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        }
    }
}
