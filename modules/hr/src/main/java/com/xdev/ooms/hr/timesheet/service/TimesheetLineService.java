package com.xdev.ooms.hr.timesheet.service;

import com.xdev.ooms.hr.common.HrRelationResolver;
import com.xdev.ooms.hr.timesheet.dto.TimesheetLineDto;
import com.xdev.ooms.hr.timesheet.entity.TimesheetLine;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

import static com.xdev.ooms.hr.common.HrActionMappings.addCrudIfActive;

@Service
public class TimesheetLineService extends BaseServiceImpl<TimesheetLine, TimesheetLineDto, TimesheetLineDto> {

    private final HrRelationResolver hrRelationResolver;

    public TimesheetLineService(
            BaseRepository<TimesheetLine> repository,
            ModelMapper modelMapper,
            HrRelationResolver hrRelationResolver
    ) {
        super(repository, modelMapper);
        this.hrRelationResolver = hrRelationResolver;
    }

    @Override
    public void resolveEntityRelations(TimesheetLine entity) {
        entity.setTimesheet(hrRelationResolver.resolveTimesheet(entity.getTimesheet()));
        entity.setPointage(hrRelationResolver.resolvePointage(entity.getPointage()));
    }

    @Override
    public Set<Action> actionsMapping(TimesheetLine line) {
        Set<Action> actions = new HashSet<>();
        addCrudIfActive(actions, line);
        return actions;
    }
}
