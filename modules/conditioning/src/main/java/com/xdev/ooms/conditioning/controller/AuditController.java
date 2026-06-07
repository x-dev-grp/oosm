package com.xdev.ooms.conditioning.controller;

import com.xdev.ooms.sharedkernel.dtos.AuditDto;
import com.xdev.ooms.conditioning.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ordreConditionement/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<AuditDto>> getAllAudits() {
        return ResponseEntity.ok(auditService.getAllAudits());
    }
}
