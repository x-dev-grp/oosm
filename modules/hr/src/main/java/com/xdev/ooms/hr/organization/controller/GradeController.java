package com.xdev.ooms.hr.organization.controller;

import com.xdev.ooms.hr.organization.dto.GradeDto;
import com.xdev.ooms.hr.organization.entity.Grade;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/grades")
public class GradeController extends BaseControllerImpl<Grade, GradeDto, GradeDto> {

    public GradeController(BaseService<Grade, GradeDto, GradeDto> baseService, ModelMapper modelMapper) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "GRADE";
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
