package com.xdev.ooms.conditioning.sync.controller;

import com.xdev.ooms.conditioning.ordrefabrication.dto.OrdreFabricationDto;
import com.xdev.ooms.conditioning.ordrefabrication.entity.OrdreFabrication;
import com.xdev.ooms.conditioning.sync.dto.SyncRequestDto;
import com.xdev.ooms.conditioning.ordrefabrication.service.OFService;
import com.xdev.ooms.conditioning.sync.service.SyncService;
import com.xdev.ooms.sharedkernel.apiDTOs.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ordreConditionement/mobile")
public class MobileSyncController {

    private final SyncService syncService;


    public MobileSyncController(SyncService syncService, OFService ofService) {
        this.syncService = syncService;
    }

    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<OrdreFabrication, OrdreFabricationDto>> syncOperation(@RequestBody SyncRequestDto request) {
        try {
            syncService.processSync(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Synchronisation réussie", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

}