package com.xdev.ooms.production.millmachine.controller;

import com.xdev.ooms.production.millmachine.dto.MillMachineDto;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;




import com.xdev.ooms.production.millmachine.entity.MillMachine;
import com.xdev.ooms.production.unifieddelivery.service.UnifiedDeliveryService;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production/millers")
public class MillMachineController extends BaseControllerImpl<MillMachine, MillMachineDto, MillMachineDto> {

    private final UnifiedDeliveryService UnifiedDeliveryService;

    public MillMachineController(BaseService<MillMachine, MillMachineDto, MillMachineDto> baseService, ModelMapper modelMapper, UnifiedDeliveryService UnifiedDeliveryService) {
        super(baseService, modelMapper);
        this.UnifiedDeliveryService = UnifiedDeliveryService;
    }
    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
    @Override
    protected String getResourceName() {
        return "MillMachine".toUpperCase();
    }
}
