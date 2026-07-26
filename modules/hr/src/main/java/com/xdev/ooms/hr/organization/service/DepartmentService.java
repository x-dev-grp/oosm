package com.xdev.ooms.hr.organization.service;

import com.xdev.ooms.hr.common.HrRelationResolver;
import com.xdev.ooms.hr.organization.dto.DepartmentDto;
import com.xdev.ooms.hr.organization.entity.Department;
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
public class DepartmentService extends BaseServiceImpl<Department, DepartmentDto, DepartmentDto> {

    private final HrRelationResolver hrRelationResolver;

    public DepartmentService(
            BaseRepository<Department> repository,
            ModelMapper modelMapper,
            HrRelationResolver hrRelationResolver
    ) {
        super(repository, modelMapper);
        this.hrRelationResolver = hrRelationResolver;
    }

    @Override
    public void resolveEntityRelations(Department entity) {
        entity.setParentDepartment(hrRelationResolver.resolveDepartment(entity.getParentDepartment()));
    }

    @Override
    public Set<Action> actionsMapping(Department department) {
        Set<Action> actions = new HashSet<>();
        addCrudIfActive(actions, department);
        if (isActive(department)) {
            actions.add(Action.EXPORT);
        }
        return actions;
    }
}
