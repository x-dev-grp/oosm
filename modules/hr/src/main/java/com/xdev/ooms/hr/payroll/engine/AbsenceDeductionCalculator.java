package com.xdev.ooms.hr.payroll.engine;

import com.xdev.ooms.hr.common.HrMoney;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Applies absence deduction on the gross salary path.
 */
@Component
public class AbsenceDeductionCalculator {

    public BigDecimal apply(BigDecimal grossBeforeAbsence, BigDecimal absenceDeduction) {
        BigDecimal gross = HrMoney.money(grossBeforeAbsence);
        BigDecimal absence = HrMoney.money(absenceDeduction != null ? absenceDeduction : BigDecimal.ZERO);
        BigDecimal after = gross.subtract(absence, HrMoney.MC);
        if (after.compareTo(BigDecimal.ZERO) < 0) {
            return HrMoney.zero();
        }
        return HrMoney.money(after);
    }
}
