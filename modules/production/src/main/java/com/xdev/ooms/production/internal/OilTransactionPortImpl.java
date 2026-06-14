package com.xdev.ooms.production.internal;


import com.xdev.ooms.sharedkernel.communicator.models.shared.OilTransactionDTO;
import com.xdev.ooms.production.oiltransaction.service.OilTransactionService;
 import com.xdev.ooms.sharedkernel.ports.OilTransactionPort;
import org.springframework.stereotype.Service;

@Service
public class OilTransactionPortImpl implements OilTransactionPort {

    private final OilTransactionPortImpl oilTransactionService;

    public OilTransactionPortImpl(OilTransactionPortImpl oilTransactionService) {
        this.oilTransactionService = oilTransactionService;
    }

    @Override
    public OilTransactionDTO create(OilTransactionDTO request) {
        return oilTransactionService.create(request);
    }


}
