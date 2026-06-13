package com.xdev.ooms.finance.controller;


import com.xdev.ooms.finance.dto.BaseTypeDto;
import com.xdev.ooms.finance.model.BaseType;
import com.xdev.ooms.finance.service.GenericTypeService;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController

@RequestMapping("/api/finance/types")

public class GenericTypeController extends BaseControllerImpl<BaseType, BaseTypeDto, BaseTypeDto> {
    private final GenericTypeService genericTypeService;

    public GenericTypeController(BaseService<BaseType, BaseTypeDto, BaseTypeDto> baseService, ModelMapper modelMapper, GenericTypeService genericTypeService) {
        super(baseService, modelMapper);
        this.genericTypeService = genericTypeService;
    }


    //     @PostMapping("/")
//    public ResponseEntity<ApiResponse<BaseTypeDto>> createType( @RequestBody BaseTypeDto baseType) {
//        try {
//            BaseTypeDto createdType = genericTypeService.createType(baseType);
//            ApiResponse<BaseTypeDto> response = new ApiResponse<>(true, "Type created successfully", createdType);
//            return ResponseEntity.ok(response);
//        } catch (RuntimeException e) {
//            ApiResponse<BaseTypeDto> response = new ApiResponse<>(false, "Error creating type: " + e.getMessage(), null);
//            return ResponseEntity.badRequest().body(response);
//        }
//    }
//
    // Get all types (e.g., all WasteTypes, SupplierTypes, OliveLotStatusTypes)

    @Override
    protected String getResourceName() {
        return "BaseType".toUpperCase();
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
