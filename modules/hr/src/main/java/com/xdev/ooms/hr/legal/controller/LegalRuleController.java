package com.xdev.ooms.hr.legal.controller;

import com.xdev.ooms.hr.legal.dto.LegalRuleDto;
import com.xdev.ooms.hr.legal.entity.LegalRule;
import com.xdev.ooms.hr.legal.service.LegalConfigurationSeedService;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/hr/legal-rules")
public class LegalRuleController extends BaseControllerImpl<LegalRule, LegalRuleDto, LegalRuleDto> {

    private final LegalConfigurationSeedService legalConfigurationSeedService;

    public LegalRuleController(
            BaseService<LegalRule, LegalRuleDto, LegalRuleDto> baseService,
            ModelMapper modelMapper,
            LegalConfigurationSeedService legalConfigurationSeedService
    ) {
        super(baseService, modelMapper);
        this.legalConfigurationSeedService = legalConfigurationSeedService;
    }

    @Override
    protected String getResourceName() {
        return "LEGALRULE";
    }

    /**
     * Seeds provisional Tunisian legal defaults for the current tenant (idempotent if CNSS config exists).
     */
    @PostMapping("/seed-defaults")
    public ResponseEntity<Map<String, Object>> seedDefaults() {
        try {
            Map<String, Object> result = legalConfigurationSeedService.ensureDefaultTunisianRules();
            result.put("success", true);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", e.getMessage() != null ? e.getMessage() : "Seed failed"
            ));
        }
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
