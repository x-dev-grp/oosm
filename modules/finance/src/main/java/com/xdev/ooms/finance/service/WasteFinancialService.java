package com.xdev.ooms.finance.service;

import com.xdev.ooms.finance.dto.FinancialTransactionDto;
import com.xdev.ooms.finance.model.FinancialTransaction;
import com.xdev.ooms.finance.repo.FinancialTransactionRepository;
import  com.xdev.ooms.sharedkernel.Enum.Currency;
import  com.xdev.ooms.sharedkernel.Enum.TransactionType;
import com.xdev.ooms.sharedkernel.utils.OSMLogger;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Simple service for creating waste-related financial transactions
 * Takes transaction data as-is from frontend without additional processing
 */
@Service
public class WasteFinancialService {
    
    private final FinancialTransactionRepository financialTransactionRepository;
    private final ModelMapper modelMapper;
    
    public WasteFinancialService(FinancialTransactionRepository financialTransactionRepository,
                                ModelMapper modelMapper) {
        this.financialTransactionRepository = financialTransactionRepository;
        this.modelMapper = modelMapper;
    }
    
    /**
     * Creates a financial transaction for waste operations using data as provided
     * @param transactionDto The financial transaction data from frontend
     * @return Created financial transaction
     */
    @Transactional
    public FinancialTransactionDto createWasteFinancialTransaction(FinancialTransactionDto transactionDto) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "createWasteFinancialTransaction", transactionDto);
        
        try {
            // Set defaults only if not provided
            if (transactionDto.getTransactionDate() == null) {
                transactionDto.setTransactionDate(LocalDateTime.now());
            }
            
            if (transactionDto.getCurrency() == null) {
                transactionDto.setCurrency(Currency.TND);
            }
            
            // Set waste-specific transaction type if not specified
            if (transactionDto.getTransactionType() == null) {
                transactionDto.setTransactionType(TransactionType.WASTE_SALE);
            }
            
            // Save financial transaction using provided data
            FinancialTransaction transaction = modelMapper.map(transactionDto, FinancialTransaction.class);
            transaction = financialTransactionRepository.save(transaction);
            
            FinancialTransactionDto result = modelMapper.map(transaction, FinancialTransactionDto.class);
            
            OSMLogger.logBusinessEvent(this.getClass(), "WASTE_FINANCIAL_TRANSACTION_CREATED", 
                "Created waste financial transaction - type: " + transaction.getTransactionType() + 
                ", amount: " + transaction.getAmount() + 
                ", description: " + transaction.getDescription());
            
            OSMLogger.logMethodExit(this.getClass(), "createWasteFinancialTransaction", result);
            OSMLogger.logPerformance(this.getClass(), "createWasteFinancialTransaction", startTime, System.currentTimeMillis());
            
            return result;
            
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error creating waste financial transaction", e);
            throw new RuntimeException("Failed to create waste financial transaction: " + e.getMessage(), e);
        }
    }
}