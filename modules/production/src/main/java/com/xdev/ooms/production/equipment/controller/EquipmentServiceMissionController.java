package com.xdev.ooms.production.equipment.controller;

import com.xdev.ooms.production.equipment.dto.EquipmentServiceMissionDto;
import com.xdev.ooms.production.equipment.entity.EquipmentServiceMission;
import com.xdev.ooms.production.equipment.service.EquipmentServiceMissionService;
import com.xdev.ooms.sharedkernel.apiDTOs.ApiSingleResponse;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import com.xdev.ooms.sharedkernel.utils.ExceptionHandler;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production/equipment-service-missions")
public class EquipmentServiceMissionController
        extends BaseControllerImpl<EquipmentServiceMission, EquipmentServiceMissionDto, EquipmentServiceMissionDto> {

    private final EquipmentServiceMissionService missionService;

    public EquipmentServiceMissionController(
            BaseService<EquipmentServiceMission, EquipmentServiceMissionDto, EquipmentServiceMissionDto> baseService,
            ModelMapper modelMapper,
            EquipmentServiceMissionService missionService) {
        super(baseService, modelMapper);
        this.missionService = missionService;
    }

    @Override
    public ResponseEntity<ApiSingleResponse<EquipmentServiceMission, EquipmentServiceMissionDto>> create(
            @RequestBody EquipmentServiceMissionDto dto) {
        try {
            EquipmentServiceMissionDto saved = missionService.save(dto);
            attachPermittedActions(saved);
            return ResponseEntity.ok(new ApiSingleResponse<>(true, missionService.buildSuccessMessage(saved), saved));
        } catch (Exception e) {
            return ExceptionHandler.handleSingleException(this.getClass(), "create", e);
        }
    }

    @Override
    public ResponseEntity<ApiSingleResponse<EquipmentServiceMission, EquipmentServiceMissionDto>> update(
            @RequestBody EquipmentServiceMissionDto dto) {
        try {
            EquipmentServiceMissionDto saved = missionService.update(dto);
            attachPermittedActions(saved);
            return ResponseEntity.ok(new ApiSingleResponse<>(true, missionService.buildSuccessMessage(saved), saved));
        } catch (Exception e) {
            return ExceptionHandler.handleSingleException(this.getClass(), "update", e);
        }
    }
@Override
    protected String getResourceName() {
        return "EQUIPMENTSERVICEMISSION";
    }
}
