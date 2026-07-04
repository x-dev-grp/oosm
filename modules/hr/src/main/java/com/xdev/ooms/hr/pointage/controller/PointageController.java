package com.xdev.ooms.hr.pointage.controller;

import com.xdev.ooms.hr.pointage.dto.PointageDto;
import com.xdev.ooms.hr.pointage.entity.Pointage;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/pointages")
public class PointageController extends BaseControllerImpl<Pointage, PointageDto, PointageDto> {

    public PointageController(BaseService<Pointage, PointageDto, PointageDto> baseService, ModelMapper modelMapper) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "POINTAGE";
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
