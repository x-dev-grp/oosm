package com.xdev.ooms.hr.payroll.engine;

import com.xdev.ooms.hr.common.HrMoney;
import com.xdev.ooms.hr.legal.entity.TaxBracket;
import com.xdev.ooms.hr.legal.entity.TaxConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IncomeTaxCalculatorTest {

    private IncomeTaxCalculator calculator;
    private TaxConfiguration taxConfiguration;

    @BeforeEach
    void setUp() {
        calculator = new IncomeTaxCalculator();
        taxConfiguration = new TaxConfiguration();
        List<TaxBracket> brackets = new ArrayList<>();
        brackets.add(bracket("0", "1500", "0", 1));
        brackets.add(bracket("1500", "5000", "0.15", 2));
        brackets.add(bracket("5000", "10000", "0.25", 3));
        brackets.add(bracket("10000", "20000", "0.30", 4));
        brackets.add(bracket("20000", null, "0.35", 5));
        taxConfiguration.setBrackets(brackets);
    }

    @Test
    void zeroTaxInFirstBracket() {
        BigDecimal tax = calculator.calculate(new BigDecimal("1200"), taxConfiguration);
        assertEquals(HrMoney.zero(), tax);
    }

    @Test
    void progressiveTaxInSecondBracket() {
        // taxable 1806.4 → (1806.4 - 1500) * 0.15 = 45.96
        BigDecimal tax = calculator.calculate(new BigDecimal("1806.4"), taxConfiguration);
        assertEquals(HrMoney.money(new BigDecimal("45.960")), tax);
    }

    @Test
    void resolveTaxableSalaryIsGrossMinusEmployeeCnss() {
        BigDecimal taxable = calculator.resolveTaxableSalary(
                new BigDecimal("2000"),
                new BigDecimal("193.6"));
        assertEquals(HrMoney.money(new BigDecimal("1806.400")), taxable);
    }

    @Test
    void throwsWhenTaxConfigMissing() {
        assertThrows(IllegalStateException.class, () -> calculator.calculate(BigDecimal.TEN, null));
    }

    private static TaxBracket bracket(String min, String max, String rate, int sortOrder) {
        TaxBracket b = new TaxBracket();
        b.setMinAmount(HrMoney.money(new BigDecimal(min)));
        b.setMaxAmount(max != null ? HrMoney.money(new BigDecimal(max)) : null);
        b.setRate(HrMoney.rate(new BigDecimal(rate)));
        b.setSortOrder(sortOrder);
        return b;
    }
}
