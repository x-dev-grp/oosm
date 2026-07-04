package com.xdev.ooms.finance.financialtransaction.controller;

import com.xdev.ooms.finance.financialtransaction.dto.FinancialTransactionDto;
import com.xdev.ooms.finance.financialtransaction.dto.SupplierFinancialSummaryDto;
import com.xdev.ooms.finance.financialtransaction.entity.FinancialTransaction;
import com.xdev.ooms.finance.financialtransaction.service.FinancialTransactionService;
import com.xdev.ooms.finance.financialtransaction.service.WasteFinancialService;
import com.xdev.ooms.production.supplier.repository.SupplierRepository;

import com.xdev.ooms.sharedkernel.apiDTOs.ApiSingleResponse;
import com.xdev.ooms.sharedkernel.apiDTOs.ApiResponse;
import com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/finance/transactions")
public class FinancialTransactionController extends BaseControllerImpl<FinancialTransaction, FinancialTransactionDto, FinancialTransactionDto> {

    private final FinancialTransactionService financialTransactionService;
    private final WasteFinancialService wasteFinancialService;
    private final SupplierRepository supplierRepository;

    public FinancialTransactionController(BaseService<FinancialTransaction, FinancialTransactionDto, FinancialTransactionDto> baseService,
                                          ModelMapper modelMapper,
                                          FinancialTransactionService financialTransactionService,
                                          WasteFinancialService wasteFinancialService, SupplierRepository supplierRepository) {
        super(baseService, modelMapper);
        this.financialTransactionService = financialTransactionService;
        this.wasteFinancialService = wasteFinancialService;
        this.supplierRepository = supplierRepository;
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<ApiResponse<FinancialTransaction, FinancialTransactionDto>> getBySupplier(
            @PathVariable UUID supplierId) {
        try {
            List<FinancialTransactionDto> transactions = financialTransactionService.findBySupplierId(supplierId);
            return ResponseEntity.ok(new ApiResponse<>(true,
                    "Financial transactions fetched successfully for supplier",
                    attachPermittedActions(transactions)));
        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(), "Error fetching supplier financial transactions", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Error fetching supplier financial transactions: " + e.getMessage(), null));
        }
    }

    @GetMapping("/supplier/{supplierId}/summary")
    public ResponseEntity<SupplierFinancialSummaryDto> getSupplierSummary(@PathVariable UUID supplierId) {
        try {
            return ResponseEntity.ok(financialTransactionService.getSupplierFinancialSummary(supplierId));
        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(), "Error fetching supplier financial summary", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ApiSingleResponse<FinancialTransaction, FinancialTransactionDto>> save(@RequestBody FinancialTransactionDto dto) {
        OOSMLogger.logMethodEntry(this.getClass(), "createFinancialTransaction", dto);

        try {
            normalizeSupplier(dto);
            FinancialTransactionDto created = financialTransactionService.save(dto);
            attachPermittedActions(created);

            return ResponseEntity.ok(new ApiSingleResponse<>(true, "Financial transaction created successfully", created));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiSingleResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(), "Error creating financial transaction", e);
            return ResponseEntity.internalServerError().body(new ApiSingleResponse<>(false, "Error creating financial transaction: " + e.getMessage(), null));
        }
    }
    
    // Standard POST endpoint for Feign client compatibility
    @PostMapping
    public ResponseEntity<ApiSingleResponse<FinancialTransaction, FinancialTransactionDto>> create(@RequestBody FinancialTransactionDto dto) {
        return save(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiSingleResponse<FinancialTransaction, FinancialTransactionDto>> updateById(
            @PathVariable UUID id,
            @RequestBody FinancialTransactionDto dto) {
        dto.setId(id);
        return update(dto);
    }

    @Override
    public ResponseEntity<ApiSingleResponse<FinancialTransaction, FinancialTransactionDto>> update(@RequestBody FinancialTransactionDto dto) {
        try {
            normalizeSupplier(dto);
            FinancialTransactionDto updated = financialTransactionService.update(dto);
            attachPermittedActions(updated);
            return ResponseEntity.ok(new ApiSingleResponse<>(true, "Financial transaction updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiSingleResponse<>(false, e.getMessage(), null));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(), "Error updating financial transaction", e);
            return ResponseEntity.internalServerError().body(new ApiSingleResponse<>(false, "Error updating financial transaction: " + e.getMessage(), null));
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiSingleResponse<FinancialTransaction, FinancialTransactionDto>> approve(
            @PathVariable UUID id,
            Authentication authentication) {
        try {
            FinancialTransactionDto approved = financialTransactionService.approve(id, currentUser(authentication));
            attachPermittedActions(approved);
            return ResponseEntity.ok(new ApiSingleResponse<>(true, "Financial transaction approved successfully", approved));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(), "Error approving financial transaction", e);
            return ResponseEntity.internalServerError().body(new ApiSingleResponse<>(false, "Error approving financial transaction: " + e.getMessage(), null));
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiSingleResponse<FinancialTransaction, FinancialTransactionDto>> reject(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication) {
        try {
            String reason = body == null ? null : body.get("reason");
            FinancialTransactionDto rejected = financialTransactionService.reject(id, currentUser(authentication), reason);
            attachPermittedActions(rejected);
            return ResponseEntity.ok(new ApiSingleResponse<>(true, "Financial transaction rejected successfully", rejected));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(), "Error rejecting financial transaction", e);
            return ResponseEntity.internalServerError().body(new ApiSingleResponse<>(false, "Error rejecting financial transaction: " + e.getMessage(), null));
        }
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }

    @Override
    protected String getResourceName() {
        return "FinancialTransaction".toUpperCase();
    }

    private void normalizeSupplier(FinancialTransactionDto dto) {
        if (dto == null || dto.getSupplier() == null) {
            return;
        }
        SupplierDto supplierDto = dto.getSupplier();
        if (supplierDto.getId() == null) {
            return;
        }
        supplierRepository.findByIdAndIsDeletedFalse(supplierDto.getId())
                .map(supplier -> modelMapper.map(supplier, SupplierDto.class))
                .ifPresent(dto::setSupplier);
    }

    private String currentUser(Authentication authentication) {
        if (authentication == null) {
            return "System";
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            Object preferredUsername = Optional.ofNullable(jwt.getClaim("preferred_username"))
                    .orElse(jwt.getClaim("username"));
            if (preferredUsername != null) {
                return preferredUsername.toString();
            }
        }
        return authentication.getName() == null || authentication.getName().isBlank()
                ? "System"
                : authentication.getName();
    }
    
    /**
     * Creates a waste-specific financial transaction
     * Takes the transaction data as-is from frontend without additional processing
     * @param dto The financial transaction data
     * @return Created financial transaction
     */
    @PostMapping("/waste")
    public ResponseEntity<ApiSingleResponse<FinancialTransaction, FinancialTransactionDto>> createWasteTransaction(@RequestBody FinancialTransactionDto dto) {
        OOSMLogger.logMethodEntry(this.getClass(), "createWasteTransaction", dto);

        try {
            if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(new ApiSingleResponse<>(false, "Invalid amount: must be greater than 0", null));
            }
            
            // Create waste financial transaction using provided data
            FinancialTransactionDto created = wasteFinancialService.createWasteFinancialTransaction(dto);

            return ResponseEntity.ok(new ApiSingleResponse<>(true, "Waste financial transaction created successfully", created));

        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(), "Error creating waste financial transaction", e);
            return ResponseEntity.internalServerError().body(new ApiSingleResponse<>(false, "Error creating waste transaction: " + e.getMessage(), null));
        }
    }

} 
