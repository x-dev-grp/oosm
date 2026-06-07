package com.xdev.ooms.production.controller;


import com.xdev.ooms.production.dto.*;
import com.xdev.ooms.production.model.QualityControlRule;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production/qualitycontrolrules")
public class QualityControlRuleController extends BaseControllerImpl<QualityControlRule, QualityControlRuleDto, QualityControlRuleDto> {
    public QualityControlRuleController(BaseService<QualityControlRule, QualityControlRuleDto, QualityControlRuleDto> baseService, ModelMapper modelMapper) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "QualityControlRule".toUpperCase();
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}