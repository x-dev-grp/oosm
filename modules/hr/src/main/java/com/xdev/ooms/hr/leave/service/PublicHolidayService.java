package com.xdev.ooms.hr.leave.service;

import com.xdev.ooms.hr.leave.dto.PublicHolidayDto;
import com.xdev.ooms.hr.leave.entity.PublicHoliday;
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
public class PublicHolidayService extends BaseServiceImpl<PublicHoliday, PublicHolidayDto, PublicHolidayDto> {

    public PublicHolidayService(BaseRepository<PublicHoliday> repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
    }

    @Override
    public Set<Action> actionsMapping(PublicHoliday entity) {
        Set<Action> actions = new HashSet<>();
        addCrudIfActive(actions, entity);
        if (isActive(entity)) {
            actions.add(Action.EXPORT);
        }
        return actions;
    }
}
