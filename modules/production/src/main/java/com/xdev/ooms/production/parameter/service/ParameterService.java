package com.xdev.ooms.production.parameter.service;


import com.xdev.ooms.production.parameter.config.ProductionParameterDefault;
import com.xdev.ooms.production.parameter.config.ProductionParameterDefaults;
import com.xdev.ooms.production.parameter.dto.ParameterDto;
import com.xdev.ooms.production.parameter.entity.Parameter;
import com.xdev.ooms.production.parameter.repository.ParameterRepo;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ParameterService extends BaseServiceImpl<Parameter, ParameterDto, ParameterDto> {

    private static final String SYSTEM_USER = "system";

    private final ParameterRepo parameterRepo;

    protected ParameterService(BaseRepository<Parameter> repository, ModelMapper modelMapper, ParameterRepo parameterRepo) {
        super(repository, modelMapper);
        this.parameterRepo = parameterRepo;
    }

    @Transactional
    public Parameter getByCode(String code, UUID tenantId) {
        ensureDefaultsForTenant(tenantId);
        return parameterRepo.findByTenantIdAndCode(tenantId, code)
                .orElseThrow(() -> new EntityNotFoundException("Parameter not found: " + code));
    }

    @Override
    @Transactional
    public List<ParameterDto> findAll() {
        ensureDefaultsForTenant(TenantContext.getCurrentTenant());
        return super.findAll();
    }

    @Transactional
    public void ensureDefaultsForTenant(UUID tenantId) {
        seedDefaultsForTenant(tenantId);
    }

    /**
     * Creates any missing catalog defaults for the tenant. Existing values are left untouched.
     */
    @Transactional
    public ParameterSeedResult seedDefaultsForTenant(UUID tenantId) {
        if (tenantId == null) {
            return new ParameterSeedResult(0, 0, ProductionParameterDefaults.all().size());
        }

        int created = 0;
        int alreadyPresent = 0;
        for (ProductionParameterDefault definition : ProductionParameterDefaults.all()) {
            if (parameterRepo.findByTenantIdAndCode(tenantId, definition.code()).isPresent()) {
                alreadyPresent++;
            } else {
                createDefaultParameter(definition, tenantId);
                created++;
            }
        }
        return new ParameterSeedResult(created, alreadyPresent, ProductionParameterDefaults.all().size());
    }

    public record ParameterSeedResult(int created, int alreadyPresent, int catalogSize) {
    }

    private Parameter createDefaultParameter(ProductionParameterDefault definition, UUID tenantId) {
        Parameter parameter = new Parameter();
        parameter.setTenantId(tenantId);
        parameter.setCode(definition.code());
        parameter.setCategory(definition.category());
        parameter.setValue(definition.defaultValue());
        parameter.setType(definition.type());
        parameter.setDescription(definition.description());
        parameter.setCreatedBy(SYSTEM_USER);
        return parameterRepo.save(parameter);
    }

    @Override
    public Set<Action> actionsMapping(Parameter millMachine) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ));
        return actions;
    }
}
