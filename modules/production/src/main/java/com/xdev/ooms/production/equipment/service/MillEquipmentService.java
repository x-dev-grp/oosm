package com.xdev.ooms.production.equipment.service;

import com.xdev.ooms.production.equipment.dto.MillEquipmentDto;
import com.xdev.ooms.production.equipment.entity.MillEquipment;
import com.xdev.ooms.production.equipment.enums.MillEquipmentStatus;
import com.xdev.ooms.production.equipment.repository.MillEquipmentRepository;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import jakarta.validation.ValidationException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class MillEquipmentService extends BaseServiceImpl<MillEquipment, MillEquipmentDto, MillEquipmentDto> {

    private final MillEquipmentRepository millEquipmentRepository;

    public MillEquipmentService(MillEquipmentRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.millEquipmentRepository = repository;
    }

    @Override
    public Set<Action> actionsMapping(MillEquipment equipment) {
        Set<Action> actions = new HashSet<>();
        actions.add(Action.READ);
        actions.add(Action.UPDATE);
        actions.add(Action.DELETE);
        return actions;
    }

    @Override
    public MillEquipmentDto save(MillEquipmentDto request) {
        validateRequest(request);
        if (request.getStatus() == null) {
            request.setStatus(MillEquipmentStatus.AVAILABLE);
        }
        if (request.getHoursOperated() == null) {
            request.setHoursOperated(0d);
        }
        if (request.getDefaultHourlyRate() == null) {
            request.setDefaultHourlyRate(0d);
        }
        return super.save(request);
    }

    @Override
    public MillEquipmentDto update(MillEquipmentDto request) {
        validateRequest(request);
        return super.update(request);
    }

    private void validateRequest(MillEquipmentDto request) {
        if (request == null) {
            throw new ValidationException("Equipment payload is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ValidationException("Equipment name is required");
        }
        if (request.getEquipmentType() == null) {
            throw new ValidationException("Equipment type is required");
        }
    }
}
