package com.xdev.ooms.production.internal;


import com.xdev.ooms.sharedkernel.communicator.models.shared.OilTransactionDTO;
import com.xdev.ooms.production.oiltransaction.service.OilTransactionService;
 import com.xdev.ooms.sharedkernel.ports.OilTransactionPort;
import org.springframework.stereotype.Service;

@Service
public class OilTransactionPortImpl implements OilTransactionPort {

    private final OilTransactionService oilTransactionService;
    private final ProductionModuleDtoMapper dtoMapper;

    public OilTransactionPortImpl(OilTransactionService oilTransactionService, ProductionModuleDtoMapper dtoMapper) {
        this.oilTransactionService = oilTransactionService;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public OilTransactionDTO create(OilTransactionDTO request) {
        return dtoMapper.toShared(oilTransactionService.save(dtoMapper.fromShared(request)));
    }


}
