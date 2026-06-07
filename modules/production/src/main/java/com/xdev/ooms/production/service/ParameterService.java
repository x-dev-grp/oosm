package com.xdev.ooms.production.service;


import com.xdev.ooms.production.dto.*;
import com.xdev.ooms.production.model.Parameter;
import com.xdev.ooms.production.repository.ParameterRepo;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class ParameterService extends BaseServiceImpl<Parameter, ParameterDto, ParameterDto> {

    private final ParameterRepo parameterRepo;

    protected ParameterService(BaseRepository<Parameter> repository, ModelMapper modelMapper, ParameterRepo parameterRepo) {
        super(repository, modelMapper);
        this.parameterRepo = parameterRepo;
    }
    public Parameter getByCode(String code, UUID tenantId) {
        Parameter param = parameterRepo.findByTenantIdAndCode(tenantId,code)
                .orElseThrow(() -> new EntityNotFoundException("Parameter not found: " + code));
        return modelMapper.map(param,Parameter.class);
    }
    @Override
    public Set<Action> actionsMapping(Parameter millMachine) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ));
        return actions;
    }
}
