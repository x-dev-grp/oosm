package com.xdev.ooms.production.maintenance.controller;

import com.xdev.ooms.production.maintenance.dto.MaintenanceWorkOrderDto;
import com.xdev.ooms.production.maintenance.entity.MaintenanceWorkOrder;
import com.xdev.ooms.production.maintenance.service.MaintenanceWorkOrderService;
import com.xdev.ooms.sharedkernel.apiDTOs.ApiSingleResponse;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.utils.ExceptionHandler;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production/maintenance-work-orders")
public class MaintenanceWorkOrderController extends BaseControllerImpl<MaintenanceWorkOrder, MaintenanceWorkOrderDto, MaintenanceWorkOrderDto> {

    private final MaintenanceWorkOrderService maintenanceWorkOrderService;

    public MaintenanceWorkOrderController(
            BaseService<MaintenanceWorkOrder, MaintenanceWorkOrderDto, MaintenanceWorkOrderDto> baseService,
            ModelMapper modelMapper,
            MaintenanceWorkOrderService maintenanceWorkOrderService) {
        super(baseService, modelMapper);
        this.maintenanceWorkOrderService = maintenanceWorkOrderService;
    }

    @Override
    public ResponseEntity<ApiSingleResponse<MaintenanceWorkOrder, MaintenanceWorkOrderDto>> create(
            @RequestBody MaintenanceWorkOrderDto dto) {
        try {
            MaintenanceWorkOrderDto saved = maintenanceWorkOrderService.save(dto);
            attachPermittedActions(saved);
            String message = maintenanceWorkOrderService.buildSuccessMessage(saved);
            return ResponseEntity.ok(new ApiSingleResponse<>(true, message, saved));
        } catch (Exception e) {
            return ExceptionHandler.handleSingleException(this.getClass(), "create", e);
        }
    }

    @Override
    public ResponseEntity<ApiSingleResponse<MaintenanceWorkOrder, MaintenanceWorkOrderDto>> update(
            @RequestBody MaintenanceWorkOrderDto dto) {
        try {
            MaintenanceWorkOrderDto saved = maintenanceWorkOrderService.update(dto);
            attachPermittedActions(saved);
            String message = maintenanceWorkOrderService.buildSuccessMessage(saved);
            return ResponseEntity.ok(new ApiSingleResponse<>(true, message, saved));
        } catch (Exception e) {
            return ExceptionHandler.handleSingleException(this.getClass(), "update", e);
        }
    }
@Override
    protected String getResourceName() {
        return "MAINTENANCEWORKORDER";
    }
}
