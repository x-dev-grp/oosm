package com.xdev.ooms.finance.internal;

import com.xdev.ooms.sharedkernel.Enum.ExpenseCategory;
import com.xdev.ooms.sharedkernel.Enum.PaymentMethod;
import com.xdev.ooms.sharedkernel.ports.ExpensePort;
import com.xdev.ooms.sharedkernel.ports.ExpenseRecordCommand;
import com.xdev.ooms.sharedkernel.ports.PayrollAccountingCommand;
import com.xdev.ooms.sharedkernel.ports.PayrollAccountingPort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Records payroll employer cost as a paid expense via {@link ExpensePort}.
 * Idempotency is expected to be enforced by the HR caller (financePosted flag);
 * the external reference embeds the payroll period id for audit.
 */
@Service
public class PayrollAccountingPortImpl implements PayrollAccountingPort {

    private final ExpensePort expensePort;

    public PayrollAccountingPortImpl(ExpensePort expensePort) {
        this.expensePort = expensePort;
    }

    @Override
    public String postPayroll(PayrollAccountingCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Payroll accounting command is required");
        }
        BigDecimal amount = command.totalEmployerCost() != null
                ? command.totalEmployerCost()
                : command.net();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payroll employer cost must be greater than zero");
        }

        String label = command.periodLabel() != null ? command.periodLabel() : "Payroll";
        String notes = buildNotes(command);
        String externalRef = command.idempotencyKey() != null
                ? command.idempotencyKey()
                : (command.payrollPeriodId() != null ? command.payrollPeriodId().toString() : null);

        return expensePort.record(new ExpenseRecordCommand(
                "Payroll — " + label,
                amount.doubleValue(),
                ExpenseCategory.OTHER,
                PaymentMethod.TRANSFER,
                "Payroll employer cost " + label,
                "PAYROLL",
                notes,
                externalRef
        ));
    }

    private String buildNotes(PayrollAccountingCommand command) {
        return "Payroll accounting"
                + " | period=" + nullSafe(command.periodLabel())
                + " | gross=" + nullSafe(command.gross())
                + " | deductions=" + nullSafe(command.deductions())
                + " | net=" + nullSafe(command.net())
                + " | employerContributions=" + nullSafe(command.employerContributions())
                + " | totalEmployerCost=" + nullSafe(command.totalEmployerCost())
                + " | periodId=" + nullSafe(command.payrollPeriodId());
    }

    private static String nullSafe(Object value) {
        return value == null ? "-" : value.toString();
    }
}
