package com.xdev.ooms.hr.advance.service;

import com.xdev.ooms.hr.advance.dto.SalaryAdvanceDto;
import com.xdev.ooms.hr.advance.entity.SalaryAdvance;
import com.xdev.ooms.hr.common.HrRelationResolver;
import com.xdev.ooms.hr.common.enums.SalaryAdvanceStatus;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.utils.AuditHelper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static com.xdev.ooms.hr.common.HrActionMappings.addCrudIfActive;
import static com.xdev.ooms.hr.common.HrActionMappings.isActive;

@Service
public class SalaryAdvanceService extends BaseServiceImpl<SalaryAdvance, SalaryAdvanceDto, SalaryAdvanceDto> {

    private final HrRelationResolver hrRelationResolver;

    public SalaryAdvanceService(
            BaseRepository<SalaryAdvance> repository,
            ModelMapper modelMapper,
            HrRelationResolver hrRelationResolver
    ) {
        super(repository, modelMapper);
        this.hrRelationResolver = hrRelationResolver;
    }

    @Override
    public void resolveEntityRelations(SalaryAdvance entity) {
        entity.setEmployee(hrRelationResolver.resolveEmployee(entity.getEmployee()));
    }

    @Override
    @Transactional
    public SalaryAdvanceDto save(SalaryAdvanceDto request) {
        SalaryAdvance entity = modelMapper.map(request, entityClass);
        if (entity.getStatus() == null) {
            entity.setStatus(SalaryAdvanceStatus.REQUESTED);
        }
        if (entity.getRemainingAmount() == null) {
            entity.setRemainingAmount(entity.getAmount());
        }
        resolveEntityRelations(entity);
        AuditHelper.applyAuditOnCreate(entity);
        SalaryAdvance saved = repository.save(entity);
        return modelMapper.map(saved, outDTOClass);
    }

    @Override
    public Set<Action> actionsMapping(SalaryAdvance entity) {
        Set<Action> actions = new HashSet<>();
        addCrudIfActive(actions, entity);
        if (isActive(entity) && entity.getStatus() == SalaryAdvanceStatus.REQUESTED) {
            actions.add(Action.APPROVE);
            actions.add(Action.CANCEL);
        }
        return actions;
    }
}
