package com.xdev.ooms.hr.organization.service;

import com.xdev.ooms.hr.organization.dto.GradeDto;
import com.xdev.ooms.hr.organization.entity.Grade;
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
public class GradeService extends BaseServiceImpl<Grade, GradeDto, GradeDto> {

    public GradeService(BaseRepository<Grade> repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
    }

    @Override
    public Set<Action> actionsMapping(Grade grade) {
        Set<Action> actions = new HashSet<>();
        addCrudIfActive(actions, grade);
        if (isActive(grade)) {
            actions.add(Action.EXPORT);
        }
        return actions;
    }
}
