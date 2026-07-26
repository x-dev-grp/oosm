package com.xdev.ooms.hr.overtime.service;

import com.xdev.ooms.hr.overtime.dto.OvertimeRuleDto;
import com.xdev.ooms.hr.overtime.entity.OvertimeRule;
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
public class OvertimeRuleService extends BaseServiceImpl<OvertimeRule, OvertimeRuleDto, OvertimeRuleDto> {

    public OvertimeRuleService(BaseRepository<OvertimeRule> repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
    }

    @Override
    public Set<Action> actionsMapping(OvertimeRule rule) {
        Set<Action> actions = new HashSet<>();
        addCrudIfActive(actions, rule);
        if (isActive(rule)) {
            actions.add(Action.EXPORT);
        }
        return actions;
    }
}
