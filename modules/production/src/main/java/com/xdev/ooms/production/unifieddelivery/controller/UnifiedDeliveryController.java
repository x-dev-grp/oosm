package com.xdev.ooms.production.unifieddelivery.controller;

import com.xdev.ooms.production.unifieddelivery.dto.PaymentDTO;
import com.xdev.ooms.production.unifieddelivery.dto.UnifiedDeliveryDTO;





import com.xdev.ooms.production.unifieddelivery.dto.ExchangePricingDto;
import com.xdev.ooms.production.unifieddelivery.dto.NextDeliveryNumbersDto;
import com.xdev.ooms.production.unifieddelivery.dto.NextDeliveryNumbersResponse;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;
import com.xdev.ooms.production.unifieddelivery.service.UnifiedDeliveryService;
import  com.xdev.ooms.sharedkernel.Enum.DeliveryType;
import  com.xdev.ooms.sharedkernel.Enum.OliveLotStatus;
import  com.xdev.ooms.sharedkernel.Enum.Olive_Oil_Type;

import com.xdev.ooms.sharedkernel.apiDTOs.ApiSingleResponse;
import com.xdev.ooms.sharedkernel.apiDTOs.ApiResponse;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/production/deliveries")

public class UnifiedDeliveryController extends BaseControllerImpl<UnifiedDelivery, UnifiedDeliveryDTO, UnifiedDeliveryDTO> {

    private final UnifiedDeliveryService UnifiedDeliveryService;
    private final UnifiedDeliveryService unifiedDeliveryService;

    public UnifiedDeliveryController(BaseService<UnifiedDelivery, UnifiedDeliveryDTO, UnifiedDeliveryDTO> baseService, ModelMapper modelMapper, UnifiedDeliveryService UnifiedDeliveryService, UnifiedDeliveryService unifiedDeliveryService) {
        super(baseService, modelMapper);
        this.UnifiedDeliveryService = UnifiedDeliveryService;
        this.unifiedDeliveryService = unifiedDeliveryService;
    }
    @PostMapping("/payment")
    public ResponseEntity<?> processPayment(@RequestBody PaymentDTO paymentDTO) {

        try{
            this.UnifiedDeliveryService.processPayment(paymentDTO);
            return ResponseEntity.ok(new ApiResponse<>(true, "Pricing updated successfully", null));
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/next-numbers")
    public ResponseEntity<NextDeliveryNumbersResponse> previewNextNumbers(
            @RequestParam DeliveryType deliveryType,
            @RequestParam(required = false) Olive_Oil_Type oliveType,
            @RequestParam(required = false) Olive_Oil_Type oilType) {
        NextDeliveryNumbersDto numbers = UnifiedDeliveryService.previewNextNumbers(deliveryType, oliveType, oilType);
        return ResponseEntity.ok(new NextDeliveryNumbersResponse(true, "Next delivery numbers", numbers));
    }

    @GetMapping("/planning")
    public ResponseEntity<ApiResponse<UnifiedDelivery, UnifiedDeliveryDTO>> getPlanning() {
        ApiResponse<UnifiedDelivery, UnifiedDeliveryDTO> response = new ApiResponse<>(true, "Delleveirs for planning fetched  successfully", attachPermittedActions(this.UnifiedDeliveryService.getForPlanning()));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/findForQuality")
    public ResponseEntity<ApiResponse<UnifiedDelivery, UnifiedDeliveryDTO>> getDeliveriesWithoutQualityControl(@RequestParam("types") String types) {
        List<String> typeList = Arrays.asList(types.split(","));
        ApiResponse<UnifiedDelivery, UnifiedDeliveryDTO> response = new ApiResponse<>(true, "Delleveirs for planning fetched  successfully", attachPermittedActions(this.UnifiedDeliveryService.findByDeliveryTypeInAndQualityControlResultsIsNull(typeList)));

        return ResponseEntity.ok(response);
    }

    // Get deliveries by supplier ID
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<ApiResponse<UnifiedDelivery, UnifiedDeliveryDTO>> getDeliveriesBySupplier(@PathVariable UUID supplierId) {
        ApiResponse<UnifiedDelivery, UnifiedDeliveryDTO> response = new ApiResponse<>(true, "Deliveries for supplier fetched successfully", attachPermittedActions(this.UnifiedDeliveryService.getDeliveriesBySupplier(supplierId)));
        return ResponseEntity.ok(response);
    }// Get deliveries by supplier ID
    @GetMapping("/getDeliveryByOliveLotNumber/{id}")
    public ResponseEntity<ApiSingleResponse<UnifiedDelivery, UnifiedDeliveryDTO>> getDeliveryByOliveLotNumber(@PathVariable UUID id) {
        ApiSingleResponse<UnifiedDelivery, UnifiedDeliveryDTO> response = new ApiSingleResponse<>(true, "Deliveries for supplier fetched successfully", attachPermittedActions(this.UnifiedDeliveryService.getByOliveLotNumber(id)));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getDeliveryByLotNumber/{lotNumber}")
    public ResponseEntity<ApiSingleResponse<UnifiedDelivery, UnifiedDeliveryDTO>> getDeliveryByLotNumber(@PathVariable String lotNumber) {
        ApiSingleResponse<UnifiedDelivery, UnifiedDeliveryDTO> response = new ApiSingleResponse<>(true, "Deliveries for supplier fetched successfully", attachPermittedActions(this.UnifiedDeliveryService.getByLotNumber(lotNumber)));
        return ResponseEntity.ok(response);
    }
    @GetMapping("/getDeliveriesByGlobalLotNumber/{lotNumber}")
    public ResponseEntity<ApiResponse<UnifiedDelivery, UnifiedDeliveryDTO>> getDeliveriesByGlobalLotNumber(@PathVariable String lotNumber) {
        ApiResponse<UnifiedDelivery, UnifiedDeliveryDTO> response = new ApiResponse<>(true, "Deliveries for supplier fetched successfully", attachPermittedActions(this.UnifiedDeliveryService.getDeliveriesByGlobalLotNumber(lotNumber)));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getDeliveryByLotNumber/{lotNumber}/{type}")
    public ResponseEntity<ApiSingleResponse<UnifiedDelivery, UnifiedDeliveryDTO>> getDeliveryByLotNumber(@PathVariable String lotNumber, @PathVariable DeliveryType type) {
        ApiSingleResponse<UnifiedDelivery, UnifiedDeliveryDTO> response = new ApiSingleResponse<>(true, "Deliveries for supplier fetched successfully", attachPermittedActions(this.UnifiedDeliveryService.getByLotNumberAndType(lotNumber, type)));
        return ResponseEntity.ok(response);
    }

    // Get paid deliveries by supplier ID
    @GetMapping("/supplier/{supplierId}/paid")
    public ResponseEntity<ApiResponse<UnifiedDelivery, UnifiedDeliveryDTO>> getPaidDeliveriesBySupplier(@PathVariable UUID supplierId) {
        ApiResponse<UnifiedDelivery, UnifiedDeliveryDTO> response = new ApiResponse<>(true, "Paid deliveries for supplier fetched successfully", attachPermittedActions(this.UnifiedDeliveryService.getPaidDeliveriesBySupplier(supplierId)));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/updateStatue/{id}/{status}")
    public ResponseEntity<ApiResponse<UnifiedDelivery, UnifiedDeliveryDTO>> updateStatue(@PathVariable("id") UUID id, @PathVariable("status") OliveLotStatus status, @RequestParam(value = "cause", required = false) String cause) {
        // delegate to your service
       try{
           this.UnifiedDeliveryService.updateStatus(id, status, cause);
           ApiResponse<UnifiedDelivery, UnifiedDeliveryDTO> response = new ApiResponse<>(true, "Status updated successfully", null);
           return ResponseEntity.ok(response);
       } catch (Exception e) {

           throw new RuntimeException(e);
       }


    }@GetMapping("/updateprice/{id}/{updateprice}")
    public ResponseEntity<ApiResponse<UnifiedDelivery, UnifiedDeliveryDTO>> updatePrice(@PathVariable("id") UUID id, @PathVariable("updateprice") Double unitPrice) {
        // delegate to your service
       try{
           this.UnifiedDeliveryService.updateprice(id, unitPrice);
           ApiResponse<UnifiedDelivery, UnifiedDeliveryDTO> response = new ApiResponse<>(true, "price  updated successfully", null);
           return ResponseEntity.ok(response);
       } catch (Exception e) {

           throw new RuntimeException(e);
       }


    }
    // Get unpaid deliveries by supplier ID
    @GetMapping("/supplier/{supplierId}/unpaid")
    public ResponseEntity<ApiResponse<UnifiedDelivery, UnifiedDeliveryDTO>> getUnpaidDeliveriesBySupplier(@PathVariable UUID supplierId) {
        ApiResponse<UnifiedDelivery, UnifiedDeliveryDTO> response = new ApiResponse<>(true, "Unpaid deliveries for supplier fetched successfully", attachPermittedActions(this.UnifiedDeliveryService.getUnpaidDeliveriesBySupplier(supplierId)));
        return ResponseEntity.ok(response);
    }


    @PostMapping("/update-exchange-pricing")
    public ResponseEntity<?> updateExchangePricingAndCreateOilTransactionOut(
            @RequestBody ExchangePricingDto dto) {
      try{
          this.UnifiedDeliveryService.updateExchangePricingAndCreateOilTransactionOut(dto)   ;
          return ResponseEntity.ok(new ApiResponse<>(true, "Pricing updated successfully", null));
      }catch (Exception e) {
          throw new RuntimeException(e);
      }

    }
    @PostMapping("/update-payment-pricing")
    public ResponseEntity<?> updatePrincingForPaymentreception(
            @RequestBody ExchangePricingDto dto) {
      try{
          this.UnifiedDeliveryService.updatePrincingForPaymentreception(dto)   ;
          return ResponseEntity.ok(new ApiResponse<>(true, "Pricing updated successfully", null));
      }catch (Exception e) {
          throw new RuntimeException(e);
      }

    }

    @GetMapping("/createOilRecFromOliveRec/{uuid}")
    public ResponseEntity<ApiSingleResponse<UnifiedDelivery, UnifiedDeliveryDTO>> createOilRecFromOliveRec(
            @PathVariable UUID uuid) {
        try {
            UnifiedDelivery oilDelivery = UnifiedDeliveryService.createOilRecFromOliveRecImpl(uuid, false, null);
            UnifiedDeliveryDTO dto = modelMapper.map(oilDelivery, UnifiedDeliveryDTO.class);
            return ResponseEntity.ok(new ApiSingleResponse<>(true, "Oil reception created", attachPermittedActions(dto)));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiSingleResponse<>(false, e.getMessage(), null));
        }
    }

    @Override
    protected String getResourceName() {
        return "UnifiedDelivery".toUpperCase();
    }

    @Override
    public ResponseEntity<ApiSingleResponse<UnifiedDelivery, UnifiedDeliveryDTO>> findDtoByUuid(UUID id) {
        // Get the base response from the superclass
        ResponseEntity<ApiSingleResponse<UnifiedDelivery, UnifiedDeliveryDTO>> base = super.findDtoByUuid(id);
        ApiSingleResponse<UnifiedDelivery, UnifiedDeliveryDTO> body = base.getBody();
        if (body == null || body.getData() == null) {
            return base;
        }

        UnifiedDeliveryDTO u = body.getData();

        // Ensure QC list is initialized
        if (u.getQualityControlResults() == null) {
            u.setQualityControlResults(new HashSet<>());
        }

        // If we have an olive lot number, try to fetch its oil reception and merge QC results
        String lotOliveNumber = u.getLotOliveNumber();
        if (lotOliveNumber != null && !lotOliveNumber.isBlank()) {
            UUID oliveLotUuid = null;
            try {
                oliveLotUuid = UUID.fromString(lotOliveNumber);
            } catch (IllegalArgumentException ignore) {
                // not a UUID; skip lookup
            }

            if (oliveLotUuid != null) {
                UnifiedDeliveryDTO oilReception = unifiedDeliveryService.getByOliveLotNumber(oliveLotUuid);
                if (oilReception != null && oilReception.getQualityControlResults() != null) {
                    u.getQualityControlResults().addAll(oilReception.getQualityControlResults());
                }
            }
        }

        // Return the modified payload
        return ResponseEntity.ok(body);
    }
}
