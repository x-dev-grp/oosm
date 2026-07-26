package com.xdev.ooms.hr.payroll.engine;

import com.xdev.ooms.hr.common.HrMoney;
import com.xdev.ooms.hr.legal.entity.TaxBracket;
import com.xdev.ooms.hr.legal.entity.TaxConfiguration;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * Progressive IRPP from {@link TaxConfiguration} brackets (never hardcoded).
 * v1 taxable income = gross − employee CNSS.
 */
@Component
public class IncomeTaxCalculator {

    public BigDecimal calculate(BigDecimal taxableIncome, TaxConfiguration taxConfiguration) {
        if (taxConfiguration == null) {
            throw new IllegalStateException(
                    "Tax configuration is missing. Seed legal rules via POST /api/hr/legal-rules/seed-defaults");
        }
        List<TaxBracket> brackets = taxConfiguration.getBrackets();
        if (brackets == null || brackets.isEmpty()) {
            throw new IllegalStateException(
                    "Tax brackets are missing. Seed legal rules via POST /api/hr/legal-rules/seed-defaults");
        }

        BigDecimal taxable = HrMoney.money(
                taxableIncome != null && taxableIncome.compareTo(BigDecimal.ZERO) > 0
                        ? taxableIncome
                        : BigDecimal.ZERO);

        List<TaxBracket> ordered = brackets.stream()
                .sorted(Comparator
                        .comparing(TaxBracket::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(b -> b.getMinAmount() != null ? b.getMinAmount() : BigDecimal.ZERO))
                .toList();

        BigDecimal tax = HrMoney.zero();
        for (TaxBracket bracket : ordered) {
            tax = HrMoney.money(tax.add(taxForBracket(taxable, bracket), HrMoney.MC));
        }
        return tax;
    }

    /**
     * v1: taxable = gross − employeeCnss.
     */
    public BigDecimal resolveTaxableSalary(BigDecimal grossSalary, BigDecimal employeeCnss) {
        BigDecimal gross = HrMoney.money(grossSalary);
        BigDecimal cnss = HrMoney.money(employeeCnss);
        BigDecimal taxable = gross.subtract(cnss, HrMoney.MC);
        if (taxable.compareTo(BigDecimal.ZERO) < 0) {
            return HrMoney.zero();
        }
        return HrMoney.money(taxable);
    }

    private BigDecimal taxForBracket(BigDecimal taxable, TaxBracket bracket) {
        BigDecimal min = bracket.getMinAmount() != null ? bracket.getMinAmount() : BigDecimal.ZERO;
        if (taxable.compareTo(min) <= 0) {
            return HrMoney.zero();
        }
        BigDecimal upper = bracket.getMaxAmount() != null
                ? taxable.min(bracket.getMaxAmount())
                : taxable;
        BigDecimal slice = upper.subtract(min, HrMoney.MC);
        if (slice.compareTo(BigDecimal.ZERO) <= 0) {
            return HrMoney.zero();
        }
        BigDecimal rate = bracket.getRate() != null ? HrMoney.rate(bracket.getRate()) : HrMoney.rate(BigDecimal.ZERO);
        BigDecimal sliceTax = slice.multiply(rate, HrMoney.MC);
        BigDecimal fixed = bracket.getFixedAmount() != null ? bracket.getFixedAmount() : BigDecimal.ZERO;
        return HrMoney.money(sliceTax.add(fixed, HrMoney.MC));
    }
}
