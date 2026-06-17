package com.xdev.ooms.finance.billing.service;

import java.math.BigDecimal;

/**
 * Standard VAT rates per the Tunisian VAT Code (Code de la TVA), Article 7.
 * Default commercial rate: 19%. Reduced rates 7% and 13% apply to listed goods/services.
 */
public final class TunisiaVatDefaults {

    public static final BigDecimal STANDARD_RATE = new BigDecimal("19");
    public static final BigDecimal REDUCED_RATE_7 = new BigDecimal("7");
    public static final BigDecimal REDUCED_RATE_13 = new BigDecimal("13");

    public static final String LEGAL_MENTION =
            "Facture établie conformément à l'article 18 du Code de la TVA tunisien. "
                    + "Prix unitaires hors TVA. Taux et montants de TVA mentionnés par ligne.";

    private TunisiaVatDefaults() {
    }

    public static BigDecimal resolveRate(BigDecimal requested) {
        return requested == null ? STANDARD_RATE : requested;
    }
}
