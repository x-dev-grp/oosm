package com.xdev.ooms.production.parameter.config;

import com.xdev.ooms.sharedkernel.Enum.ParameterType;

import java.util.List;
import java.util.Optional;

public final class ProductionParameterDefaults {

    public static final String PRODUCTION_CATEGORY = "PRODUCTION";
    public static final String FINANCE_CATEGORY = "FINANCE";

    private static final List<ProductionParameterDefault> DEFAULTS = List.of(
            new ProductionParameterDefault(
                    "OLIVE_UNIT_PRICE",
                    PRODUCTION_CATEGORY,
                    ParameterType.DOUBLE,
                    "3.50",
                    "Default price per KG of olives"
            ),
            new ProductionParameterDefault(
                    "DAILY_OIL_METRIC",
                    PRODUCTION_CATEGORY,
                    ParameterType.STRING,
                    "{\"current\":0,\"history\":[]}",
                    "Daily BASE oil unit price reference (current and history JSON)"
            ),
            new ProductionParameterDefault(
                    "PRIX_TRITURATION_KG",
                    FINANCE_CATEGORY,
                    ParameterType.DOUBLE,
                    "0",
                    "Milling price per kg (TND)"
            )
    );

    private ProductionParameterDefaults() {
    }

    public static List<ProductionParameterDefault> all() {
        return DEFAULTS;
    }

    public static Optional<ProductionParameterDefault> find(String code) {
        return DEFAULTS.stream()
                .filter(definition -> definition.code().equals(code))
                .findFirst();
    }
}
