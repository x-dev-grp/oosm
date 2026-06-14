package com.xdev.ooms.sharedkernel.basetype.controller;

import com.xdev.ooms.sharedkernel.Enum.TypeCategory;
import com.xdev.ooms.sharedkernel.apiDTOs.ApiResponse;
import com.xdev.ooms.sharedkernel.basetype.dto.BaseTypeDto;
import com.xdev.ooms.sharedkernel.basetype.entity.BaseType;
import com.xdev.ooms.sharedkernel.basetype.service.GenericTypeService;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/types", "/api/production/types", "/api/finance/types"})
public class GenericTypeController extends BaseControllerImpl<BaseType, BaseTypeDto, BaseTypeDto> {
    private final GenericTypeService service;

    public GenericTypeController(GenericTypeService service, ModelMapper modelMapper) {
        super(service, modelMapper);
        this.service = service;
    }

    @GetMapping("/{type}")
    public ResponseEntity<ApiResponse<BaseType, BaseTypeDto>> getAllTypes(@PathVariable TypeCategory type) {
        List<BaseTypeDto> types = service.getAllTypes(type).stream()
                .map(entity -> modelMapper.map(entity, BaseTypeDto.class))
                .toList();
        return ResponseEntity.ok(new ApiResponse<>(true, "Types fetched successfully", types));
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }

    @Override
    protected String getResourceName() {
        return "BASE_TYPE";
    }
}
