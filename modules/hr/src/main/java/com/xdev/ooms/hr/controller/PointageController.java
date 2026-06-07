package com.xdev.ooms.hr.controller;

import com.xdev.ooms.hr.Dtos.PointageDto;
import com.xdev.ooms.hr.model.Pointage;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/pointage")
public class PointageController extends BaseControllerImpl<Pointage, PointageDto, PointageDto> {


    public PointageController(BaseService<Pointage, PointageDto, PointageDto> baseService, ModelMapper modelMapper) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "POINTAGE";
    }
}
