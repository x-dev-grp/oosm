package com.xdev.ooms.sharedkernel.basetype.controller;

import com.xdev.ooms.sharedkernel.Enum.TypeCategory;
import com.xdev.ooms.sharedkernel.apiDTOs.ApiResponse;
import com.xdev.ooms.sharedkernel.basetype.dto.BaseTypeDto;
import com.xdev.ooms.sharedkernel.basetype.entity.BaseType;
import com.xdev.ooms.sharedkernel.basetype.service.BaseTypeProvisioningService;
import com.xdev.ooms.sharedkernel.basetype.service.GenericTypeService;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping({"/api/types", "/api/production/types", "/api/finance/types"})
public class GenericTypeController extends BaseControllerImpl<BaseType, BaseTypeDto, BaseTypeDto> {
    private final GenericTypeService service;
    private final BaseTypeProvisioningService provisioningService;

    public GenericTypeController(
            GenericTypeService service,
            ModelMapper modelMapper,
            BaseTypeProvisioningService provisioningService
    ) {
        super(service, modelMapper);
        this.service = service;
        this.provisioningService = provisioningService;
    }

    @GetMapping("/{type}")
    public ResponseEntity<ApiResponse<BaseType, BaseTypeDto>> getAllTypes(@PathVariable TypeCategory type) {
        List<BaseTypeDto> types = service.getAllTypes(type).stream()
                .map(entity -> modelMapper.map(entity, BaseTypeDto.class))
                .toList();
        return ResponseEntity.ok(new ApiResponse<>(true, "Types fetched successfully", types));
    }

    @PostMapping("/provision-defaults")
    public ResponseEntity<Map<String, Object>> provisionDefaults() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Tenant context is required"
            ));
        }
        int created = provisioningService.provisionTunisiaDefaults(tenantId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "created", created,
                "message", created > 0 ? "Default Tunisia base types provisioned" : "Base types already present"
        ));
    }
@Override
    protected String getResourceName() {
        return "BASE_TYPE";
    }
}
