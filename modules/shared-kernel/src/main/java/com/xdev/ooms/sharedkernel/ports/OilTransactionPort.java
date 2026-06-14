package com.xdev.ooms.sharedkernel.ports;

import com.xdev.ooms.sharedkernel.communicator.models.shared.OilTransactionDTO;

/**
 * Cross-module port: finance creates oil transactions in production.
 */
public interface OilTransactionPort {

    OilTransactionDTO create(OilTransactionDTO request);
}
