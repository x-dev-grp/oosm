package com.xdev.ooms.production.service;


import com.xdev.ooms.production.dto.*;
import com.xdev.ooms.production.model.MachinePlan;
import com.xdev.ooms.production.repository.MachinePlanRepository;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class MachinePlanService extends BaseServiceImpl<MachinePlan, MachinePlanDto, MachinePlanDto> {

    private final MachinePlanRepository machinePlanRepository;

    public MachinePlanService(MachinePlanRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.machinePlanRepository = repository;
    }

    // Add any machine-plan–specific business logic here if needed.

    @Override
    public Set<Action> actionsMapping(MachinePlan machinePlan) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ));
        return actions;
    }
}
