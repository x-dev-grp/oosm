package com.xdev.ooms.finance.controller;


import com.xdev.ooms.finance.dto.SupplierDto;
import com.xdev.ooms.finance.model.Supplier;
import com.xdev.ooms.finance.service.SupplierTypeService;
import com.xdev.ooms.sharedkernel.apiDTOs.ApiSingleResponse;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance/suppliers_type")
public class SupplierTypeController extends BaseControllerImpl<Supplier, SupplierDto, SupplierDto> {

    private final SupplierTypeService supplierTypeService;

    public SupplierTypeController(BaseService<Supplier, SupplierDto, SupplierDto> baseService, ModelMapper modelMapper, SupplierTypeService supplierTypeService) {
        super(baseService, modelMapper);
        this.supplierTypeService = supplierTypeService;
    }


    @Override
    protected String getResourceName() {
        return "Supplier".toUpperCase();
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
