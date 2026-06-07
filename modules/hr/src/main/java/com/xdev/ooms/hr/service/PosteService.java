package com.xdev.ooms.hr.service;

import com.xdev.ooms.hr.Dtos.DepartmentDto;
import com.xdev.ooms.hr.Dtos.PosteDto;
import com.xdev.ooms.hr.model.Department;
import com.xdev.ooms.hr.model.Poste;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.utils.OSMLogger;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class PosteService extends BaseServiceImpl<Poste, PosteDto, PosteDto> {

    public PosteService(BaseRepository<Poste> repository, ModelMapper modelMapper) {
        super(repository, modelMapper);


    }
    @Override
    public Set<Action> actionsMapping(Poste Poste) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "actionsMapping",Poste);
        Set<Action> actions = new HashSet<>();
        actions.add(Action.UPDATE);
        actions.add(Action.DELETE);
        OSMLogger.logMethodExit(this.getClass(), "actionsMapping", actions);
        OSMLogger.logPerformance(this.getClass(), "actionsMapping", startTime, System.currentTimeMillis());
        return actions;
    }




}