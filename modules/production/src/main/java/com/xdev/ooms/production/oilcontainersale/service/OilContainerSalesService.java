package com.xdev.ooms.production.oilcontainersale.service;


import com.xdev.ooms.production.oilcontainersale.dto.OilContainerSaleDto;



import com.xdev.ooms.production.oilcontainersale.entity.OilContainerSale;
import com.xdev.ooms.production.oilcontainersale.repository.OilContainerSaleRepo;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class OilContainerSalesService extends BaseServiceImpl<OilContainerSale, OilContainerSaleDto, OilContainerSaleDto> {

    public OilContainerSalesService(OilContainerSaleRepo repository, ModelMapper modelMapper ) {
        super(repository, modelMapper);
     }
    @Override
    public Set<Action> actionsMapping(OilContainerSale oilContainerSale) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ));
        return actions;
    }

}
