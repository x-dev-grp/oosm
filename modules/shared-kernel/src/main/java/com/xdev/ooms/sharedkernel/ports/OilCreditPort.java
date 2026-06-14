package com.xdev.ooms.sharedkernel.ports;

import java.util.UUID;

/**
 * Cross-module port: production approves oil credits managed by finance.
 */
public interface OilCreditPort {

    void approveOilCredit(UUID transactionId);
}
