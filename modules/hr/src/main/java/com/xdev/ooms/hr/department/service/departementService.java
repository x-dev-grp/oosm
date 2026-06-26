package com.xdev.ooms.hr.department.service;

import com.xdev.ooms.hr.department.dto.DepartmentDto;
import com.xdev.ooms.hr.department.entity.Department;
import com.xdev.ooms.hr.employee.entity.Employee;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class departementService extends BaseServiceImpl<Department, DepartmentDto, DepartmentDto> {

    public departementService(BaseRepository<Department> repository, ModelMapper modelMapper) {
        super(repository, modelMapper);


    }
    @Override
    public Set<Action> actionsMapping(Department Department) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "actionsMapping", Department);
        Set<Action> actions = new HashSet<>();
        actions.add(Action.READ);
        actions.add(Action.UPDATE);
        actions.add(Action.DELETE);
        actions.add(Action.PAY);
        OOSMLogger.logMethodExit(this.getClass(), "actionsMapping", actions);
        OOSMLogger.logPerformance(this.getClass(), "actionsMapping", startTime, System.currentTimeMillis());
        return actions;
    }




}