package com.xdev.ooms.production.controller;


import com.xdev.ooms.production.dto.*;
import com.xdev.ooms.production.model.Parameter;
import com.xdev.ooms.production.service.ParameterService;
import com.xdev.ooms.sharedkernel.apiDTOs.ApiResponse;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/production/parameter")
public class ParameterController extends BaseControllerImpl<Parameter, ParameterDto, ParameterDto> {
    public static final String X_TENANT_ID = "X-Tenant-ID";
    private final ParameterService parameterService;

    public ParameterController(BaseService<Parameter, ParameterDto, ParameterDto> baseService, ModelMapper modelMapper, ParameterService parameterService) {
        super(baseService, modelMapper);
        this.parameterService = parameterService;
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<Parameter, ParameterDto>> getByCode(@PathVariable String code, @RequestHeader(X_TENANT_ID) UUID tenantId // or resolve from token
    ) {
         ApiResponse<Parameter, ParameterDto> ff = new ApiResponse<>(true, "", List.of(modelMapper.map(parameterService.getByCode(code, tenantId), ParameterDto.class)));

        return ResponseEntity.ok(ff);
    }

    @Override
    protected String getResourceName() {
        return "Parameter".toUpperCase();
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
