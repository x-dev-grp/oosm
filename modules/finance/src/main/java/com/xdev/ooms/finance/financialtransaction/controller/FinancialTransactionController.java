package com.xdev.ooms.finance.financialtransaction.controller;

import com.xdev.ooms.finance.financialtransaction.dto.FinancialTransactionDto;
import com.xdev.ooms.finance.financialtransaction.entity.FinancialTransaction;
import com.xdev.ooms.finance.financialtransaction.service.FinancialTransactionService;
import com.xdev.ooms.finance.financialtransaction.service.WasteFinancialService;
import com.xdev.ooms.production.supplier.repository.SupplierRepository;

import com.xdev.ooms.sharedkernel.apiDTOs.ApiSingleResponse;
import com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto;
import com.xdev.ooms.sharedkernel.controllers.impl.BaseControllerImpl;
import com.xdev.ooms.sharedkernel.services.BaseService;
import com.xdev.ooms.sharedkernel.utils.OSMLogger;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

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

    @PostMapping("/create")
    public ResponseEntity<ApiSingleResponse<FinancialTransaction, FinancialTransactionDto>> save(@RequestBody FinancialTransactionDto dto) {
        OSMLogger.logMethodEntry(this.getClass(), "createFinancialTransaction", dto);

        try {
            // Basic validation: amount must be > 0
            if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(new ApiSingleResponse<>(false, "Invalid amount: must be greater than 0", null));
            }
            // (Optional) other validations, e.g. paymentMethod, transactionType, etc.
            if (dto.getPaymentMethod() == null) {
                return ResponseEntity.badRequest().body(new ApiSingleResponse<>(false, "Payment method is required", null));
            }

            // Delegate to service
            var supplier = supplierRepository.findByExternalIdAndIsDeletedFalse(dto.getsupplier().getExternalId());
            dto.setsupplier(modelMapper.map(supplier, SupplierDto.class));
            FinancialTransactionDto created = financialTransactionService.save(dto);

            return ResponseEntity.ok(new ApiSingleResponse<>(true, "Financial transaction created successfully", created));

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error creating financial transaction", e);
            return ResponseEntity.internalServerError().body(new ApiSingleResponse<>(false, "Error creating financial transaction: " + e.getMessage(), null));
        }
    }
    
    // Standard POST endpoint for Feign client compatibility
    @PostMapping
    public ResponseEntity<ApiSingleResponse<FinancialTransaction, FinancialTransactionDto>> create(@RequestBody FinancialTransactionDto dto) {
        return save(dto);
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }

    @Override
    protected String getResourceName() {
        return "FinancialTransaction".toUpperCase();
    }
    
    /**
     * Creates a waste-specific financial transaction
     * Takes the transaction data as-is from frontend without additional processing
     * @param dto The financial transaction data
     * @return Created financial transaction
     */
    @PostMapping("/waste")
    public ResponseEntity<ApiSingleResponse<FinancialTransaction, FinancialTransactionDto>> createWasteTransaction(@RequestBody FinancialTransactionDto dto) {
        OSMLogger.logMethodEntry(this.getClass(), "createWasteTransaction", dto);

        try {
            // Basic validation: amount must be > 0
            if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(new ApiSingleResponse<>(false, "Invalid amount: must be greater than 0", null));
            }
            
            // Create waste financial transaction using provided data
            FinancialTransactionDto created = wasteFinancialService.createWasteFinancialTransaction(dto);

            return ResponseEntity.ok(new ApiSingleResponse<>(true, "Waste financial transaction created successfully", created));

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error creating waste financial transaction", e);
            return ResponseEntity.internalServerError().body(new ApiSingleResponse<>(false, "Error creating waste transaction: " + e.getMessage(), null));
        }
    }

} 