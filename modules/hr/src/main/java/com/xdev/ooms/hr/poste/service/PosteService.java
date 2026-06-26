package com.xdev.ooms.hr.poste.service;

import com.xdev.ooms.hr.department.dto.DepartmentDto;
import com.xdev.ooms.hr.poste.dto.PosteDto;
import com.xdev.ooms.hr.department.entity.Department;
import com.xdev.ooms.hr.poste.entity.Poste;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
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
        OOSMLogger.logMethodEntry(this.getClass(), "actionsMapping",Poste);
        Set<Action> actions = new HashSet<>();
        actions.add(Action.UPDATE);
        actions.add(Action.DELETE);
        OOSMLogger.logMethodExit(this.getClass(), "actionsMapping", actions);
        OOSMLogger.logPerformance(this.getClass(), "actionsMapping", startTime, System.currentTimeMillis());
        return actions;
    }




}