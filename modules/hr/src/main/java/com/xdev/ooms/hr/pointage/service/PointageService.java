package com.xdev.ooms.hr.pointage.service;

import com.xdev.ooms.hr.common.HrBusinessLinkageService;
import com.xdev.ooms.hr.common.HrRelationResolver;
import com.xdev.ooms.hr.common.enums.AttendanceSource;
import com.xdev.ooms.hr.pointage.component.AttendanceAnomalyDetector;
import com.xdev.ooms.hr.pointage.dto.PointageDto;
import com.xdev.ooms.hr.pointage.entity.Pointage;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.utils.AuditHelper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static com.xdev.ooms.hr.common.HrActionMappings.addCrudIfActive;
import static com.xdev.ooms.hr.common.HrActionMappings.isActive;

@Service
public class PointageService extends BaseServiceImpl<Pointage, PointageDto, PointageDto> {

    private final HrRelationResolver hrRelationResolver;
    private final HrBusinessLinkageService hrBusinessLinkage;
    private final AttendanceAnomalyDetector attendanceAnomalyDetector;

    public PointageService(
            BaseRepository<Pointage> repository,
            ModelMapper modelMapper,
            HrRelationResolver hrRelationResolver,
            HrBusinessLinkageService hrBusinessLinkage,
            AttendanceAnomalyDetector attendanceAnomalyDetector
    ) {
        super(repository, modelMapper);
        this.hrRelationResolver = hrRelationResolver;
        this.hrBusinessLinkage = hrBusinessLinkage;
        this.attendanceAnomalyDetector = attendanceAnomalyDetector;
    }

    @Override
    public void resolveEntityRelations(Pointage entity) {
        entity.setEmployee(hrRelationResolver.resolveEmployee(entity.getEmployee()));
    }

    @Override
    @Transactional
    public PointageDto save(PointageDto request) {
        Pointage entity = modelMapper.map(request, entityClass);
        resolveEntityRelations(entity);
        if (entity.getSource() == null) {
            entity.setSource(AttendanceSource.MANUAL);
        }
        hrBusinessLinkage.validateAndEnrichPointage(entity, null);
        applyAnomalies(entity);
        AuditHelper.applyAuditOnCreate(entity);
        Pointage saved = repository.save(entity);
        return modelMapper.map(saved, outDTOClass);
    }

    @Override
    @Transactional
    public PointageDto update(PointageDto request) {
        if (request == null || request.getId() == null) {
            return null;
        }
        Pointage existing = repository.findById(request.getId()).orElse(null);
        if (existing == null) {
            return null;
        }
        AuditHelper.applyAuditOnCreate(existing);
        modelMapper.map(request, existing);
        resolveEntityRelations(existing);
        hrBusinessLinkage.validateAndEnrichPointage(existing, existing.getId());
        applyAnomalies(existing);
        Pointage updated = repository.save(existing);
        return modelMapper.map(updated, outDTOClass);
    }

    private void applyAnomalies(Pointage entity) {
        entity.setAnomalyCodes(attendanceAnomalyDetector.detectAsCsv(entity));
    }

    @Override
    public Set<Action> actionsMapping(Pointage pointage) {
        Set<Action> actions = new HashSet<>();
        addCrudIfActive(actions, pointage);
        if (isActive(pointage)) {
            actions.add(Action.GEN_PDF);
            actions.add(Action.EXPORT);
        }
        return actions;
    }
}
