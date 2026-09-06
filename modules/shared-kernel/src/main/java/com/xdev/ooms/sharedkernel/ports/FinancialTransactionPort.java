package com.xdev.ooms.sharedkernel.ports;

import com.xdev.ooms.sharedkernel.Enum.ResourceName;
import com.xdev.ooms.sharedkernel.communicator.models.shared.FinancialTransactionDto;

/**
 * Cross-module port: production records ledger entries through finance.
 */
public interface FinancialTransactionPort {

    void record(FinancialTransactionDto request);

    /**
     * Posts reversing ledger rows for all non-reversal transactions linked to a document.
     *
     * @return number of reversal rows created
     */
    int reverseLinked(String externalTransactionId, ResourceName resourceName);
}
