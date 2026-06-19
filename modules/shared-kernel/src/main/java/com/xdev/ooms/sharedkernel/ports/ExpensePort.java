package com.xdev.ooms.sharedkernel.ports;

/**
 * Cross-module port: production records purchase expenses through finance.
 */
public interface ExpensePort {

    /**
     * Creates an approved expense and linked outbound financial transaction.
     *
     * @return generated invoice / bill reference
     */
    String record(ExpenseRecordCommand command);
}
