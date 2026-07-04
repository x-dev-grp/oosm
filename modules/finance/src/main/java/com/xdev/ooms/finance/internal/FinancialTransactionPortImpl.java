package com.xdev.ooms.finance.internal;

import com.xdev.ooms.finance.bankaccount.repository.BankAccountRepository;
import com.xdev.ooms.finance.financialtransaction.service.FinancialTransactionService;
import com.xdev.ooms.production.supplier.repository.SupplierRepository;
import com.xdev.ooms.sharedkernel.communicator.models.shared.FinancialTransactionDto;
import com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto;
import com.xdev.ooms.sharedkernel.ports.FinancialTransactionPort;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class FinancialTransactionPortImpl implements FinancialTransactionPort {

    private final FinancialTransactionService financialTransactionService;
    private final SupplierRepository supplierRepository;
    private final BankAccountRepository bankAccountRepository;
    private final FinanceModuleDtoMapper moduleDtoMapper;
    private final ModelMapper modelMapper;

    public FinancialTransactionPortImpl(
            FinancialTransactionService financialTransactionService,
            SupplierRepository supplierRepository,
            BankAccountRepository bankAccountRepository,
            FinanceModuleDtoMapper moduleDtoMapper,
            ModelMapper modelMapper) {
        this.financialTransactionService = financialTransactionService;
        this.supplierRepository = supplierRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.moduleDtoMapper = moduleDtoMapper;
        this.modelMapper = modelMapper;
    }

    @Override
    public void record(FinancialTransactionDto request) {
        validate(request);
        resolveSupplier(request);
        resolveBankAccount(request);
        request.setSyncProductionState(false);
        financialTransactionService.recordShared(request);
    }

    private void validate(FinancialTransactionDto request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid amount: must be greater than 0");
        }
        if (request.getPaymentMethod() == null) {
            throw new IllegalArgumentException("Payment method is required");
        }
    }

    private void resolveSupplier(FinancialTransactionDto request) {
        if (request.getsupplier() == null || request.getsupplier().getId() == null) {
            return;
        }
        var supplier = supplierRepository
                .findByIdAndIsDeletedFalse(request.getsupplier().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Supplier not found: " + request.getsupplier().getId()));
        request.setsupplier(modelMapper.map(supplier, SupplierDto.class));
    }

    private void resolveBankAccount(FinancialTransactionDto request) {
        if (request.getBankAccount() == null || request.getBankAccount().getId() == null) {
            return;
        }
        var bankAccount = bankAccountRepository
                .findByIdAndIsDeletedFalse(request.getBankAccount().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Bank account not found: " + request.getBankAccount().getId()));
        request.setBankAccount(moduleDtoMapper.toSharedBankAccount(bankAccount));
    }
}
