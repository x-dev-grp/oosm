package com.xdev.ooms.sharedkernel.ports;

/**
 * Cross-module port: finance pushes invoice/payment state to production entities.
 */
public interface ProductionFinancialSyncPort {

    void syncFromFinancialTransaction(ProductionFinancialSyncCommand command);
}
