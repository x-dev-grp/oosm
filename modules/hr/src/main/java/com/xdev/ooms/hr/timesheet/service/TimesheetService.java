package com.xdev.ooms.hr.timesheet.service;

import com.xdev.ooms.hr.common.HrPermissionSupport;
import com.xdev.ooms.hr.common.HrRelationResolver;
import com.xdev.ooms.hr.common.enums.TimesheetStatus;
import com.xdev.ooms.hr.timesheet.dto.TimesheetDto;
import com.xdev.ooms.hr.timesheet.dto.TimesheetLineDto;
import com.xdev.ooms.hr.timesheet.entity.Timesheet;
import com.xdev.ooms.hr.timesheet.entity.TimesheetLine;
import com.xdev.ooms.hr.timesheet.repository.TimesheetLineRepository;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.utils.AuditHelper;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.xdev.ooms.hr.common.HrActionMappings.addCrudIfActive;
import static com.xdev.ooms.hr.common.HrActionMappings.addRead;
import static com.xdev.ooms.hr.common.HrActionMappings.isActive;

@Service
public class TimesheetService extends BaseServiceImpl<Timesheet, TimesheetDto, TimesheetDto> {

    private final HrRelationResolver hrRelationResolver;
    private final TimesheetLineRepository timesheetLineRepository;

    public TimesheetService(
            BaseRepository<Timesheet> repository,
            ModelMapper modelMapper,
            HrRelationResolver hrRelationResolver,
            TimesheetLineRepository timesheetLineRepository
    ) {
        super(repository, modelMapper);
        this.hrRelationResolver = hrRelationResolver;
        this.timesheetLineRepository = timesheetLineRepository;
    }

    @Override
    public void resolveEntityRelations(Timesheet entity) {
        entity.setEmployee(hrRelationResolver.resolveEmployee(entity.getEmployee()));
    }

    @Override
    @Transactional(readOnly = true)
    public TimesheetDto findById(UUID id) {
        Timesheet entity = repository.findById(id).orElse(null);
        if (entity == null) {
            return null;
        }
        entity.setLines(null);
        TimesheetDto dto = modelMapper.map(entity, outDTOClass);
        dto.setLines(mapLines(id));
        return dto;
    }

    @Override
    @Transactional
    public TimesheetDto save(TimesheetDto request) {
        List<TimesheetLineDto> incomingLines = request != null ? request.getLines() : null;
        if (request != null) {
            request.setLines(null);
        }
        Timesheet entity = modelMapper.map(request, entityClass);
        entity.setLines(null);
        if (entity.getStatus() == null) {
            entity.setStatus(TimesheetStatus.DRAFT);
        }
        resolveEntityRelations(entity);
        AuditHelper.applyAuditOnCreate(entity);
        Timesheet saved = repository.save(entity);
        persistLines(saved, incomingLines);
        TimesheetDto dto = modelMapper.map(saved, outDTOClass);
        dto.setLines(mapLines(saved.getId()));
        return dto;
    }

    @Override
    @Transactional
    public TimesheetDto update(TimesheetDto request) {
        if (request == null || request.getId() == null) {
            return null;
        }
        List<TimesheetLineDto> incomingLines = request.getLines();
        request.setLines(null);
        Timesheet existing = repository.findById(request.getId()).orElse(null);
        if (existing == null) {
            return null;
        }
        existing.setLines(null);
        AuditHelper.applyAuditOnCreate(existing);
        modelMapper.map(request, existing);
        existing.setLines(null);
        resolveEntityRelations(existing);
        Timesheet updated = repository.save(existing);
        if (incomingLines != null) {
            persistLines(updated, incomingLines);
        }
        TimesheetDto dto = modelMapper.map(updated, outDTOClass);
        dto.setLines(mapLines(updated.getId()));
        return dto;
    }

    @Transactional
    public TimesheetDto validate(UUID id) {
        HrPermissionSupport.requireAction("TIMESHEET", Action.VALIDATE);
        Timesheet timesheet = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Timesheet not found: " + id));
        timesheet.setStatus(TimesheetStatus.VALIDATED);
        timesheet.setValidatedAt(LocalDateTime.now());
        timesheet.setValidatedBy(currentUsername());
        AuditHelper.applyAuditOnCreate(timesheet);
        Timesheet saved = repository.save(timesheet);
        TimesheetDto dto = modelMapper.map(saved, outDTOClass);
        dto.setLines(mapLines(saved.getId()));
        return dto;
    }

    private void persistLines(Timesheet timesheet, List<TimesheetLineDto> lineDtos) {
        if (lineDtos == null || lineDtos.isEmpty()) {
            return;
        }
        for (TimesheetLineDto lineDto : lineDtos) {
            TimesheetLine line = modelMapper.map(lineDto, TimesheetLine.class);
            line.setTimesheet(timesheet);
            line.setPointage(hrRelationResolver.resolvePointage(line.getPointage()));
            AuditHelper.applyAuditOnCreate(line);
            timesheetLineRepository.save(line);
        }
    }

    private List<TimesheetLineDto> mapLines(UUID timesheetId) {
        return timesheetLineRepository.findByTimesheet_IdAndIsDeletedFalseOrderByDateAsc(timesheetId)
                .stream()
                .map(line -> {
                    TimesheetLineDto lineDto = modelMapper.map(line, TimesheetLineDto.class);
                    lineDto.setTimesheet(null);
                    return lineDto;
                })
                .toList();
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    @Override
    public Set<Action> actionsMapping(Timesheet timesheet) {
        Set<Action> actions = new HashSet<>();
        addRead(actions);
        if (!isActive(timesheet)) {
            return actions;
        }
        TimesheetStatus status = timesheet.getStatus() != null ? timesheet.getStatus() : TimesheetStatus.DRAFT;
        if (status == TimesheetStatus.DRAFT || status == TimesheetStatus.SUBMITTED || status == TimesheetStatus.REJECTED) {
            addCrudIfActive(actions, timesheet);
            if (status == TimesheetStatus.SUBMITTED || status == TimesheetStatus.DRAFT) {
                actions.add(Action.VALIDATE);
            }
        } else if (status == TimesheetStatus.VALIDATED) {
            actions.add(Action.EXPORT);
            actions.add(Action.GEN_PDF);
        }
        return actions;
    }
}
