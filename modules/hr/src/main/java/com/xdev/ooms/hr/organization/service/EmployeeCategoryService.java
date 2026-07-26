package com.xdev.ooms.hr.organization.service;

import com.xdev.ooms.hr.organization.dto.EmployeeCategoryDto;
import com.xdev.ooms.hr.organization.entity.EmployeeCategory;
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
public class EmployeeCategoryService extends BaseServiceImpl<EmployeeCategory, EmployeeCategoryDto, EmployeeCategoryDto> {

    public EmployeeCategoryService(BaseRepository<EmployeeCategory> repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
    }

    @Override
    public Set<Action> actionsMapping(EmployeeCategory category) {
        Set<Action> actions = new HashSet<>();
        addCrudIfActive(actions, category);
        if (isActive(category)) {
            actions.add(Action.EXPORT);
        }
        return actions;
    }
}
