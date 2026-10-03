package com.xdev.ooms.production.genealogy.controller;



import com.xdev.ooms.production.genealogy.dto.GenealogyDto;
import com.xdev.ooms.production.genealogy.service.GenealogyService;
import com.xdev.ooms.sharedkernel.communicator.models.shared.ApiResponse;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.utils.PermissionSupport;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/production/traceability")
public class GenealogyController {

    private final GenealogyService genealogyService;

    public GenealogyController(GenealogyService genealogyService) {
        this.genealogyService = genealogyService;
    }

    @GetMapping("/genealogy/{storageUnitId}")
    public ResponseEntity<ApiResponse<GenealogyDto>> getGenealogy(@PathVariable UUID storageUnitId) {
        PermissionSupport.requireAction("STORAGEUNIT", Action.READ);
        GenealogyDto data = genealogyService.getFullGenealogy(storageUnitId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Genealogy fetched successfully", data));
    }
}
