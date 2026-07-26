package com.xdev.ooms.sharedkernel.ports;

/**
 * Cross-module port: HR posts payroll cost into finance when a period is validated.
 */
public interface PayrollAccountingPort {

    /**
     * Posts payroll accounting entries. Implementations must be idempotent when
     * {@link PayrollAccountingCommand#idempotencyKey()} is reused.
     *
     * @return finance reference (invoice / transaction id)
     */
    String postPayroll(PayrollAccountingCommand command);
}
