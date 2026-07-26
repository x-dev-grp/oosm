package com.xdev.ooms.hr.overtime.service;

import com.xdev.ooms.hr.common.HrPermissionSupport;
import com.xdev.ooms.hr.common.HrRelationResolver;
import com.xdev.ooms.hr.common.enums.OvertimeRequestStatus;
import com.xdev.ooms.hr.overtime.dto.OvertimeRequestDto;
import com.xdev.ooms.hr.overtime.entity.OvertimeRequest;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.utils.AuditHelper;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.xdev.ooms.hr.common.HrActionMappings.addRead;
import static com.xdev.ooms.hr.common.HrActionMappings.isActive;

@Service
public class OvertimeRequestService extends BaseServiceImpl<OvertimeRequest, OvertimeRequestDto, OvertimeRequestDto> {

    private final HrRelationResolver hrRelationResolver;

    public OvertimeRequestService(
            BaseRepository<OvertimeRequest> repository,
            ModelMapper modelMapper,
            HrRelationResolver hrRelationResolver
    ) {
        super(repository, modelMapper);
        this.hrRelationResolver = hrRelationResolver;
    }

    @Override
    public void resolveEntityRelations(OvertimeRequest entity) {
        entity.setEmployee(hrRelationResolver.resolveEmployee(entity.getEmployee()));
    }

    @Override
    @Transactional
    public OvertimeRequestDto save(OvertimeRequestDto request) {
        OvertimeRequest entity = modelMapper.map(request, entityClass);
        if (entity.getStatus() == null) {
            entity.setStatus(OvertimeRequestStatus.REQUESTED);
        }
        resolveEntityRelations(entity);
        AuditHelper.applyAuditOnCreate(entity);
        OvertimeRequest saved = repository.save(entity);
        return modelMapper.map(saved, outDTOClass);
    }

    @Transactional
    public OvertimeRequestDto approve(UUID id) {
        HrPermissionSupport.requireAction("OVERTIMEREQUEST", Action.APPROVE);
        OvertimeRequest request = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Overtime request not found: " + id));
        if (request.getStatus() != OvertimeRequestStatus.REQUESTED) {
            throw new IllegalStateException("Only REQUESTED overtime can be approved");
        }
        request.setStatus(OvertimeRequestStatus.APPROVED);
        AuditHelper.applyAuditOnCreate(request);
        OvertimeRequest saved = repository.save(request);
        return modelMapper.map(saved, outDTOClass);
    }

    @Transactional
    public OvertimeRequestDto reject(UUID id) {
        HrPermissionSupport.requireAction("OVERTIMEREQUEST", Action.REJECT);
        OvertimeRequest request = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Overtime request not found: " + id));
        if (request.getStatus() != OvertimeRequestStatus.REQUESTED) {
            throw new IllegalStateException("Only REQUESTED overtime can be rejected");
        }
        request.setStatus(OvertimeRequestStatus.REJECTED);
        AuditHelper.applyAuditOnCreate(request);
        OvertimeRequest saved = repository.save(request);
        return modelMapper.map(saved, outDTOClass);
    }

    @Override
    public Set<Action> actionsMapping(OvertimeRequest request) {
        Set<Action> actions = new HashSet<>();
        addRead(actions);
        if (!isActive(request)) {
            return actions;
        }
        OvertimeRequestStatus status = request.getStatus() != null ? request.getStatus() : OvertimeRequestStatus.REQUESTED;
        if (status == OvertimeRequestStatus.REQUESTED) {
            actions.add(Action.UPDATE);
            actions.add(Action.DELETE);
            actions.add(Action.APPROVE);
            actions.add(Action.REJECT);
            actions.add(Action.CANCEL);
        }
        return actions;
    }
}
