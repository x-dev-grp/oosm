package com.xdev.ooms.documents.commercial;

import com.xdev.ooms.documents.commercial.dto.BillLineDto;
import com.xdev.ooms.documents.commercial.dto.BillTotalsDto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class BillVatCalculator {

    private BillVatCalculator() {
    }

    public static BillTotalsDto calculateTotals(BillVatMode vatMode, Iterable<BillLineDto> lines) {
        BillTotalsDto totals = new BillTotalsDto();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal vat = BigDecimal.ZERO;

        BillVatMode mode = vatMode == null ? BillVatMode.STANDARD : vatMode;
        for (BillLineDto line : lines) {
            BigDecimal quantity = money(line.getQuantity());
            BigDecimal unitPrice = money(line.getUnitPriceExcludingVat());
            BigDecimal rate = percent(TunisiaVatDefaults.resolveRate(line.getVatRatePercent()));

            switch (mode) {
                case NONE -> subtotal = subtotal.add(quantity.multiply(unitPrice));
                case INCLUSIVE -> {
                    BigDecimal lineTtc = money(quantity.multiply(unitPrice));
                    BigDecimal lineHt = extractHtFromTtc(lineTtc, rate);
                    subtotal = subtotal.add(lineHt);
                    vat = vat.add(lineTtc.subtract(lineHt));
                }
                default -> {
                    BigDecimal lineHt = money(quantity.multiply(unitPrice));
                    subtotal = subtotal.add(lineHt);
                    vat = vat.add(lineHt.multiply(rate));
                }
            }
        }

        totals.setSubtotalExcludingVat(money(subtotal));
        totals.setVatAmount(money(vat));
        totals.setTotalIncludingVat(money(subtotal.add(vat)));
        return totals;
    }

    public static LineAmounts lineAmounts(BillVatMode vatMode, BillLineDto line) {
        BigDecimal quantity = money(line.getQuantity());
        BigDecimal unitPrice = money(line.getUnitPriceExcludingVat());
        BigDecimal rate = percent(TunisiaVatDefaults.resolveRate(line.getVatRatePercent()));
        BillVatMode mode = vatMode == null ? BillVatMode.STANDARD : vatMode;

        return switch (mode) {
            case NONE -> {
                BigDecimal lineTotal = money(quantity.multiply(unitPrice));
                yield new LineAmounts(unitPrice, lineTotal, BigDecimal.ZERO, lineTotal);
            }
            case INCLUSIVE -> {
                BigDecimal lineTtc = money(quantity.multiply(unitPrice));
                BigDecimal lineHt = extractHtFromTtc(lineTtc, rate);
                BigDecimal lineVat = money(lineTtc.subtract(lineHt));
                BigDecimal unitHt = quantity.signum() > 0
                        ? lineHt.divide(quantity, 8, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                yield new LineAmounts(unitHt, lineHt, lineVat, lineTtc);
            }
            default -> {
                BigDecimal lineHt = money(quantity.multiply(unitPrice));
                BigDecimal lineVat = money(lineHt.multiply(rate));
                yield new LineAmounts(unitPrice, lineHt, lineVat, money(lineHt.add(lineVat)));
            }
        };
    }

    public static BigDecimal extractHtFromTtc(BigDecimal lineTtc, BigDecimal rateFactor) {
        if (lineTtc == null || lineTtc.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        if (rateFactor == null || rateFactor.signum() <= 0) {
            return money(lineTtc);
        }
        return money(lineTtc.divide(BigDecimal.ONE.add(rateFactor), 8, RoundingMode.HALF_UP));
    }

    private static BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(3, RoundingMode.HALF_UP);
    }

    private static BigDecimal percent(BigDecimal value) {
        return money(value).divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
    }

    public record LineAmounts(
            BigDecimal unitPriceExcludingVat,
            BigDecimal lineHt,
            BigDecimal lineVat,
            BigDecimal lineTtc) {
    }
}
