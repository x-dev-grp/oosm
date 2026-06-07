package com.xdev.ooms.hr.service;

import com.xdev.ooms.hr.Dtos.DepartmentDto;
import com.xdev.ooms.hr.model.Department;
import com.xdev.ooms.hr.model.Employee;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.utils.OSMLogger;
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
        OSMLogger.logMethodEntry(this.getClass(), "actionsMapping", Department);
        Set<Action> actions = new HashSet<>();
        actions.add(Action.READ);
        actions.add(Action.UPDATE);
        actions.add(Action.DELETE);
        actions.add(Action.PAY);
        OSMLogger.logMethodExit(this.getClass(), "actionsMapping", actions);
        OSMLogger.logPerformance(this.getClass(), "actionsMapping", startTime, System.currentTimeMillis());
        return actions;
    }




}