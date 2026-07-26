package com.xdev.ooms.hr.leave.service;

import com.xdev.ooms.hr.common.HrRelationResolver;
import com.xdev.ooms.hr.leave.dto.LeaveBalanceDto;
import com.xdev.ooms.hr.leave.entity.LeaveBalance;
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
public class LeaveBalanceService extends BaseServiceImpl<LeaveBalance, LeaveBalanceDto, LeaveBalanceDto> {

    private final HrRelationResolver hrRelationResolver;

    public LeaveBalanceService(
            BaseRepository<LeaveBalance> repository,
            ModelMapper modelMapper,
            HrRelationResolver hrRelationResolver
    ) {
        super(repository, modelMapper);
        this.hrRelationResolver = hrRelationResolver;
    }

    @Override
    public void resolveEntityRelations(LeaveBalance entity) {
        entity.setEmployee(hrRelationResolver.resolveEmployee(entity.getEmployee()));
    }

    @Override
    public Set<Action> actionsMapping(LeaveBalance entity) {
        Set<Action> actions = new HashSet<>();
        addCrudIfActive(actions, entity);
        if (isActive(entity)) {
            actions.add(Action.EXPORT);
        }
        return actions;
    }
}
