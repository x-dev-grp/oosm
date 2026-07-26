package com.xdev.ooms.hr.common;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Money and rate helpers for HR payroll / legal calculations.
 * Prefer these over double for new monetary fields.
 */
public final class HrMoney {

    public static final MathContext MC = new MathContext(12, RoundingMode.HALF_UP);
    public static final int MONEY_SCALE = 3;
    public static final int RATE_SCALE = 6;

    private HrMoney() {
    }

    public static BigDecimal money(BigDecimal v) {
        if (v == null) {
            return zero();
        }
        return v.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal rate(BigDecimal v) {
        if (v == null) {
            return BigDecimal.ZERO.setScale(RATE_SCALE, RoundingMode.HALF_UP);
        }
        return v.setScale(RATE_SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
