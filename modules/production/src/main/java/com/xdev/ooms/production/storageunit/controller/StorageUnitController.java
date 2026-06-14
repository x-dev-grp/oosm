package com.xdev.ooms.production.storageunit.controller;

import com.xdev.ooms.production.storageunit.dto.StorageUnitDto;
import com.xdev.ooms.production.supplier.entity.Supplier;



import com.xdev.ooms.production.storageunit.entity.StorageUnit;
import com.xdev.ooms.production.storageunit.service.StorageUnitService;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/production/storage-units")

public class StorageUnitController extends BaseControllerImpl<StorageUnit, StorageUnitDto, StorageUnitDto> {
    private final StorageUnitService storageUnitService;
    public StorageUnitController(BaseService<StorageUnit, StorageUnitDto, StorageUnitDto> baseService, ModelMapper modelMapper, StorageUnitService storageUnitService) {
        super(baseService, modelMapper);
        this.storageUnitService = storageUnitService;
    }
    @PutMapping("/{storageId}/assign-supplier")
    public ResponseEntity<Void> changeSupplier(
            @PathVariable UUID storageId,
            @RequestParam(required = false) UUID supplierId) {
        storageUnitService.changeSupplier(storageId, supplierId);
        return ResponseEntity.noContent().build();
    }

    @Override
    protected String getResourceName() {
        return "STORAGEUNIT".toUpperCase();
    }
}
