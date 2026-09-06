package com.xdev.ooms.production.equipment.controller;

import com.xdev.ooms.production.equipment.dto.MillEquipmentDto;
import com.xdev.ooms.production.equipment.entity.MillEquipment;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production/mill-equipment")
public class MillEquipmentController extends BaseControllerImpl<MillEquipment, MillEquipmentDto, MillEquipmentDto> {

    public MillEquipmentController(BaseService<MillEquipment, MillEquipmentDto, MillEquipmentDto> baseService, ModelMapper modelMapper) {
        super(baseService, modelMapper);
    }
@Override
    protected String getResourceName() {
        return "MILLEQUIPMENT";
    }
}
