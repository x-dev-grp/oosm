package com.xdev.ooms.production.oilsale.controller;

import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;

import com.xdev.ooms.production.oilsale.dto.OilSaleCreateRequest;
import com.xdev.ooms.production.oilsale.dto.OilSaleDTO;
import com.xdev.ooms.production.unifieddelivery.dto.PaymentDTO;




import com.xdev.ooms.production.oilsale.entity.OilSale;
import com.xdev.ooms.production.oilsale.service.OilSaleService;
import com.xdev.ooms.sharedkernel.apiDTOs.ApiResponse;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing oil sales
 */
@RestController
@RequestMapping("/api/production/oil_sale")
public class OilSaleController extends BaseControllerImpl<OilSale, OilSaleDTO, OilSaleDTO> {

    private final OilSaleService oilSaleService;

    public OilSaleController(BaseService<OilSale, OilSaleDTO, OilSaleDTO> baseService, ModelMapper modelMapper, OilSaleService oilSaleService) {
        super(baseService, modelMapper);
        this.oilSaleService = oilSaleService;
    }

    @Override
    protected String getResourceName() {
        return "OILSALE".toUpperCase();
    }

    @PostMapping("/payment")
    public ResponseEntity<?> processPayment(@RequestBody PaymentDTO paymentDTO) {
        try {
            oilSaleService.processPayment(paymentDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing payment: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<OilSale, OilSaleDTO>> create(@RequestBody OilSaleCreateRequest request) {
        try {
            OilSaleDTO saved = oilSaleService.createWithContainers(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Pricing updated successfully", List.of(saved)));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}