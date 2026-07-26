package com.xdev.ooms.hr.leave.service;

import com.xdev.ooms.hr.common.HrBusinessLinkageService;
import com.xdev.ooms.hr.common.HrPermissionSupport;
import com.xdev.ooms.hr.common.HrRelationResolver;
import com.xdev.ooms.hr.common.enums.LeaveStatus;
import com.xdev.ooms.hr.leave.dto.LeaveRequestDto;
import com.xdev.ooms.hr.leave.entity.LeaveRequest;
import com.xdev.ooms.hr.notification.HrNotificationService;
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

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.xdev.ooms.hr.common.HrActionMappings.addRead;
import static com.xdev.ooms.hr.common.HrActionMappings.isActive;

@Service
public class LeaveRequestService extends BaseServiceImpl<LeaveRequest, LeaveRequestDto, LeaveRequestDto> {

    private final HrRelationResolver hrRelationResolver;
    private final HrBusinessLinkageService hrBusinessLinkage;
    private final HrNotificationService hrNotificationService;

    public LeaveRequestService(
            BaseRepository<LeaveRequest> repository,
            ModelMapper modelMapper,
            HrRelationResolver hrRelationResolver,
            HrBusinessLinkageService hrBusinessLinkage,
            HrNotificationService hrNotificationService
    ) {
        super(repository, modelMapper);
        this.hrRelationResolver = hrRelationResolver;
        this.hrBusinessLinkage = hrBusinessLinkage;
        this.hrNotificationService = hrNotificationService;
    }

    @Override
    public void resolveEntityRelations(LeaveRequest entity) {
        entity.setEmployee(hrRelationResolver.resolveEmployee(entity.getEmployee()));
    }

    @Override
    @Transactional
    public LeaveRequestDto save(LeaveRequestDto request) {
        LeaveRequest entity = modelMapper.map(request, entityClass);
        resolveEntityRelations(entity);
        hrBusinessLinkage.validateAndEnrichLeave(entity, null);
        AuditHelper.applyAuditOnCreate(entity);
        LeaveRequest saved = repository.save(entity);
        return modelMapper.map(saved, outDTOClass);
    }

    @Override
    @Transactional
    public LeaveRequestDto update(LeaveRequestDto request) {
        if (request == null || request.getId() == null) {
            return null;
        }
        LeaveRequest existing = repository.findById(request.getId()).orElse(null);
        if (existing == null) {
            return null;
        }
        AuditHelper.applyAuditOnCreate(existing);
        modelMapper.map(request, existing);
        resolveEntityRelations(existing);
        hrBusinessLinkage.validateAndEnrichLeave(existing, existing.getId());
        LeaveRequest updated = repository.save(existing);
        return modelMapper.map(updated, outDTOClass);
    }

    @Transactional
    public LeaveRequestDto approve(UUID id) {
        HrPermissionSupport.requireAction("LEAVEREQUEST", Action.APPROVE);
        LeaveRequest leave = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Leave request not found: " + id));
        hrBusinessLinkage.assertLeavePending(leave);
        leave.setStatus(LeaveStatus.APPROVED);
        AuditHelper.applyAuditOnCreate(leave);
        LeaveRequest saved = repository.save(leave);
        hrNotificationService.notifyLeaveApproved(saved, currentUserId(), currentDisplayName());
        return modelMapper.map(saved, outDTOClass);
    }

    @Transactional
    public LeaveRequestDto reject(UUID id) {
        HrPermissionSupport.requireAction("LEAVEREQUEST", Action.REJECT);
        LeaveRequest leave = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Leave request not found: " + id));
        hrBusinessLinkage.assertLeavePending(leave);
        leave.setStatus(LeaveStatus.REJECTED);
        AuditHelper.applyAuditOnCreate(leave);
        LeaveRequest saved = repository.save(leave);
        return modelMapper.map(saved, outDTOClass);
    }

    @Override
    public Set<Action> actionsMapping(LeaveRequest leave) {
        Set<Action> actions = new HashSet<>();
        addRead(actions);
        if (!isActive(leave)) {
            return actions;
        }
        LeaveStatus status = leave.getStatus() != null ? leave.getStatus() : LeaveStatus.PENDING;
        if (status == LeaveStatus.PENDING) {
            actions.add(Action.UPDATE);
            actions.add(Action.DELETE);
            actions.add(Action.APPROVE);
            actions.add(Action.REJECT);
            actions.add(Action.CANCEL);
        } else if (status == LeaveStatus.APPROVED) {
            actions.add(Action.GEN_PDF);
        }
        return actions;
    }

    private UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        try {
            Method getId = authentication.getPrincipal().getClass().getMethod("getId");
            Object id = getId.invoke(authentication.getPrincipal());
            if (id instanceof UUID uuid) {
                return uuid;
            }
            if (id != null) {
                return UUID.fromString(id.toString());
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    private String currentDisplayName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        return authentication.getName();
    }
}
