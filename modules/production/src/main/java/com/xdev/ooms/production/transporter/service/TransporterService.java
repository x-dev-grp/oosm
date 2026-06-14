package com.xdev.ooms.production.transporter.service;


import com.xdev.ooms.production.transporter.dto.TransporterDTO;



import com.xdev.ooms.production.transporter.entity.Transporter;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class TransporterService extends BaseServiceImpl<Transporter, TransporterDTO, TransporterDTO> {


    public TransporterService(BaseRepository<Transporter> repository, ModelMapper modelMapper) {
        super(repository, modelMapper);

    }

    @Override
    public Set<Action> actionsMapping(Transporter transporter) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ));
        return actions;
    }
}
