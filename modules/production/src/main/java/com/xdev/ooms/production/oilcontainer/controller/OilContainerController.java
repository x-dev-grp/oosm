package com.xdev.ooms.production.oilcontainer.controller;

import com.xdev.ooms.production.oilcontainer.dto.OilContainerDTO;
import com.xdev.ooms.production.oilcontainer.dto.OilContainerPurchaseRequest;
import com.xdev.ooms.production.oilcontainer.dto.OilContainerPurchaseResult;
import com.xdev.ooms.production.oilcontainer.entity.OilContainer;
import com.xdev.ooms.production.oilcontainer.service.OilContainerService;
import com.xdev.ooms.sharedkernel.apiDTOs.ApiResponse;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/production/oil_container")
public class OilContainerController extends BaseControllerImpl<OilContainer, OilContainerDTO, OilContainerDTO> {

    private final OilContainerService oilContainerService;

    public OilContainerController(
            BaseService<OilContainer, OilContainerDTO, OilContainerDTO> baseService,
            ModelMapper modelMapper,
            OilContainerService oilContainerService) {
        super(baseService, modelMapper);
        this.oilContainerService = oilContainerService;
    }

    @PostMapping("/{id}/purchase")
    public ResponseEntity<ApiResponse<OilContainer, OilContainerDTO>> purchase(
            @PathVariable UUID id,
            @RequestBody OilContainerPurchaseRequest request) {
        try {
            OilContainerPurchaseResult result = oilContainerService.purchase(id, request);
            String message = "Container purchase recorded successfully. Invoice: " + result.getInvoiceReference();
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    message,
                    List.of(result.getContainer())));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
    @Override
    protected String getResourceName() {
        return "OilContainer".toUpperCase();
    }
}
