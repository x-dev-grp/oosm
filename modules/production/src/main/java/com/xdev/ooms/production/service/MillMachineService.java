package com.xdev.ooms.production.service;


import com.xdev.ooms.production.dto.*;
import com.xdev.ooms.production.model.MillMachine;
import com.xdev.ooms.production.repository.MillMachineRepository;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class MillMachineService extends BaseServiceImpl<MillMachine, MillMachineDto, MillMachineDto> {
    private final MillMachineRepository millMachineRepository;

    public MillMachineService(MillMachineRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.millMachineRepository = repository;
    }

    @Override
    public Set<Action> actionsMapping(MillMachine millMachine) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ, Action.MAINTENANCE));
        return actions;
    }
}
