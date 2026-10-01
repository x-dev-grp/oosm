package com.xdev.ooms.production.qualitycontrol.controller;

import com.xdev.ooms.production.qualitycontrol.dto.QualityControlRuleDto;
import com.xdev.ooms.production.qualitycontrol.entity.QualityControlRule;
import com.xdev.ooms.production.qualitycontrol.service.QualityControlProvisioningService;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/production/qualitycontrolrules")
public class QualityControlRuleController extends BaseControllerImpl<QualityControlRule, QualityControlRuleDto, QualityControlRuleDto> {

    private final QualityControlProvisioningService provisioningService;

    public QualityControlRuleController(
            BaseService<QualityControlRule, QualityControlRuleDto, QualityControlRuleDto> baseService,
            ModelMapper modelMapper,
            QualityControlProvisioningService provisioningService
    ) {
        super(baseService, modelMapper);
        this.provisioningService = provisioningService;
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
        int created = provisioningService.provisionDefaultRulesForTenant(tenantId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "created", created,
                "message", created > 0 ? "Default Tunisia QC rules provisioned" : "QC rules already present"
        ));
    }

    @Override
    protected String getResourceName() {
        return "QualityControlRule".toUpperCase();
    }
}