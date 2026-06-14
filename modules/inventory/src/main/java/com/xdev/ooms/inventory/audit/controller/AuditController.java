package com.xdev.ooms.inventory.audit.controller;



import com.xdev.ooms.sharedkernel.dtos.AuditDto;
import com.xdev.ooms.inventory.audit.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/inventaire/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<AuditDto>> getAllAudits() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(auditService.getAllAudits());
    }
}