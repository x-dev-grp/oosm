package com.xdev.ooms.production.oilcontainer.service;


import com.xdev.ooms.production.oilcontainer.dto.OilContainerDTO;
import com.xdev.ooms.production.oilcontainersale.entity.OilContainerSale;



import com.xdev.ooms.production.oilcontainer.entity.OilContainer;
import com.xdev.ooms.production.oilcontainer.repository.OilContainerRepository;
import com.xdev.ooms.production.oilcontainersale.repository.OilContainerSaleRepo;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OilContainerService extends BaseServiceImpl<OilContainer, OilContainerDTO, OilContainerDTO> {
    private final OilContainerRepository oilContainerRepository;

    public OilContainerService(OilContainerRepository repository, ModelMapper modelMapper, OilContainerSaleRepo oilContainerSaleRepo) {
        super(repository, modelMapper);
        this.oilContainerRepository = repository;
    }

    @Override
    public Set<Action> actionsMapping(OilContainer OilContainer) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ));
        return actions;
    }
}
