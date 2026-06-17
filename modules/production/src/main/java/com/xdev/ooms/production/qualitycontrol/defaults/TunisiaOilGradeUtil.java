package com.xdev.ooms.production.qualitycontrol.defaults;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class TunisiaOilGradeUtil {

    public static final String EXTRA_VIERGE = "Extra Vierge";
    public static final String VIERGE = "Vierge";
    public static final String LAMPANTE = "Lampante";

    private static final Set<String> CATEGORY_VALUES = Set.of(
            EXTRA_VIERGE, VIERGE, LAMPANTE,
            "Vierge Extra", "Extra", "EXTRA_VIRGIN", "VIRGIN", "LAMPANTE"
    );

    private static final Map<String, String> CATEGORY_ALIASES = new LinkedHashMap<>();

    static {
        CATEGORY_ALIASES.put("Vierge Extra", EXTRA_VIERGE);
        CATEGORY_ALIASES.put("Extra", EXTRA_VIERGE);
        CATEGORY_ALIASES.put("EXTRA_VIRGIN", EXTRA_VIERGE);
        CATEGORY_ALIASES.put("VIRGIN", VIERGE);
        CATEGORY_ALIASES.put("LAMPANTE", LAMPANTE);
    }

    private TunisiaOilGradeUtil() {
    }

    public static boolean isOilCategoryValue(String value) {
        return value != null && CATEGORY_VALUES.contains(value.trim());
    }

    public static String normalizeCategory(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String trimmed = value.trim();
        return CATEGORY_ALIASES.getOrDefault(trimmed, trimmed);
    }

    public static Optional<String> suggestOilGrade(
            Double acidity,
            Double k232,
            Double k270,
            Double deltaK,
            Double peroxide
    ) {
        if (withinEvoo(acidity, k232, k270, deltaK, peroxide)) {
            return Optional.of(EXTRA_VIERGE);
        }
        if (withinVirgin(acidity, k232, k270, deltaK, peroxide)) {
            return Optional.of(VIERGE);
        }
        if (acidity != null || k232 != null || k270 != null || deltaK != null || peroxide != null) {
            return Optional.of(LAMPANTE);
        }
        return Optional.empty();
    }

    private static boolean withinEvoo(Double acidity, Double k232, Double k270, Double deltaK, Double peroxide) {
        return within(acidity, 0.8)
                && within(k232, 2.50)
                && within(k270, 0.22)
                && within(deltaK, 0.01)
                && within(peroxide, 20.0);
    }

    private static boolean withinVirgin(Double acidity, Double k232, Double k270, Double deltaK, Double peroxide) {
        return within(acidity, 2.0)
                && within(k232, 2.60)
                && within(k270, 0.25)
                && within(deltaK, 0.01)
                && within(peroxide, 20.0);
    }

    private static boolean within(Double value, double max) {
        return value == null || value <= max;
    }
}
