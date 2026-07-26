package com.xdev.ooms.hr.payroll.service;

import com.xdev.ooms.hr.common.HrRelationResolver;
import com.xdev.ooms.hr.payroll.dto.PayrollVariableDto;
import com.xdev.ooms.hr.payroll.entity.PayrollVariable;
import com.xdev.ooms.hr.payroll.enums.PayrollVariableStatus;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

import static com.xdev.ooms.hr.common.HrActionMappings.addCrudIfActive;
import static com.xdev.ooms.hr.common.HrActionMappings.isActive;

@Service
public class PayrollVariableService extends BaseServiceImpl<PayrollVariable, PayrollVariableDto, PayrollVariableDto> {

    private final HrRelationResolver hrRelationResolver;

    public PayrollVariableService(
            BaseRepository<PayrollVariable> repository,
            ModelMapper modelMapper,
            HrRelationResolver hrRelationResolver
    ) {
        super(repository, modelMapper);
        this.hrRelationResolver = hrRelationResolver;
    }

    @Override
    public void resolveEntityRelations(PayrollVariable entity) {
        entity.setEmployee(hrRelationResolver.resolveEmployee(entity.getEmployee()));
        if (entity.getStatus() == null) {
            entity.setStatus(PayrollVariableStatus.DRAFT);
        }
    }

    @Override
    public Set<Action> actionsMapping(PayrollVariable entity) {
        Set<Action> actions = new HashSet<>();
        addCrudIfActive(actions, entity);
        if (isActive(entity)) {
            actions.add(Action.EXPORT);
            if (entity.getStatus() == PayrollVariableStatus.DRAFT) {
                actions.add(Action.VALIDATE);
            }
        }
        return actions;
    }
}
