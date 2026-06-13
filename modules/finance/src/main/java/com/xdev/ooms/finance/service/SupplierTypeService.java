package com.xdev.ooms.finance.service;


import com.xdev.ooms.finance.dto.SupplierDto;
import com.xdev.ooms.finance.model.Supplier;
import com.xdev.ooms.finance.repo.GenericRepository;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.utils.OSMLogger;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class SupplierTypeService extends BaseServiceImpl<Supplier, SupplierDto, SupplierDto> {

    // Repository to reattach BaseType entities (for region and supplier type)
    private final GenericRepository genericRepository;

    // Constructor injection: in addition to Supplier repository and ModelMapper,
    // inject the BaseType repository.
    public SupplierTypeService(BaseRepository<Supplier> repository,
                               ModelMapper modelMapper,
                               GenericRepository genericRepository
    ) {
        super(repository, modelMapper);
        this.genericRepository = genericRepository;
    }


    @Override
    public Set<Action> actionsMapping(Supplier supplier) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "actionsMapping", supplier);

        try {
            Set<Action> actions = new HashSet<>();
            actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ));

            OSMLogger.logMethodExit(this.getClass(), "actionsMapping", "Actions: " + actions);
            OSMLogger.logPerformance(this.getClass(), "actionsMapping", startTime, System.currentTimeMillis());

            return actions;

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error mapping actions for supplier: " + supplier.getId(), e);
            throw e;
        }
    }


}
