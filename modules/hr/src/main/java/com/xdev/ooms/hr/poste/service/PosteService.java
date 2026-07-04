package com.xdev.ooms.hr.poste.service;

import com.xdev.ooms.hr.poste.dto.PosteDto;
import com.xdev.ooms.hr.poste.entity.Poste;
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
public class PosteService extends BaseServiceImpl<Poste, PosteDto, PosteDto> {

    public PosteService(BaseRepository<Poste> repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
    }

    @Override
    public Set<Action> actionsMapping(Poste poste) {
        Set<Action> actions = new HashSet<>();
        addCrudIfActive(actions, poste);
        if (isActive(poste)) {
            actions.add(Action.EXPORT);
        }
        return actions;
    }
}
