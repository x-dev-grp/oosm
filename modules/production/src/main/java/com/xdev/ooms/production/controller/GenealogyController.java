package com.xdev.ooms.production.controller;


import com.xdev.ooms.production.dto.*;
import com.xdev.ooms.production.dto.GenealogyDto;
import com.xdev.ooms.production.service.GenealogyService;
import com.xdev.ooms.production.dto.ApiResponse;
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
        GenealogyDto data = genealogyService.getFullGenealogy(storageUnitId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Genealogy fetched successfully", data));
    }
}
