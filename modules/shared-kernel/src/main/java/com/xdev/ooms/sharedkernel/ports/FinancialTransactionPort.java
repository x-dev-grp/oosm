package com.xdev.ooms.sharedkernel.ports;

import com.xdev.ooms.sharedkernel.communicator.models.shared.FinancialTransactionDto;

/**
 * Cross-module port: production records ledger entries through finance.
 */
public interface FinancialTransactionPort {

    void record(FinancialTransactionDto request);
}
