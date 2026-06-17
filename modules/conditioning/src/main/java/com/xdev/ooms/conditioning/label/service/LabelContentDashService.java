package com.xdev.ooms.conditioning.label.service;

import com.xdev.ooms.conditioning.label.dto.LabelContentDashDto;
import com.xdev.ooms.conditioning.label.entity.LabelContent;
import com.xdev.ooms.conditioning.label.repository.LabelContentRepository;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class LabelContentDashService
        extends BaseServiceImpl<LabelContent, LabelContentDashDto, LabelContentDashDto> {

    public LabelContentDashService(
            BaseRepository<LabelContent> repository,
            LabelContentRepository labelContentRepository,
            ModelMapper modelMapper
    ) {
        super(repository, modelMapper);
    }

    @Override
    public Set<Action> actionsMapping(LabelContent entity) {
        return Set.of(Action.READ, Action.UPDATE, Action.DELETE);
    }
}
