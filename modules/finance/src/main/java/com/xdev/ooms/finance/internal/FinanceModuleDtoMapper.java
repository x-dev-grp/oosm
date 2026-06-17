package com.xdev.ooms.finance.internal;

import com.xdev.ooms.finance.expense.dto.ExpenseDto;
import com.xdev.ooms.finance.bankaccount.entity.BankAccount;
import com.xdev.ooms.finance.expense.entity.Expense;
import com.xdev.ooms.sharedkernel.communicator.models.common.dtos.BaseDto;
import com.xdev.ooms.sharedkernel.communicator.models.shared.BankAccountDto;
import com.xdev.ooms.sharedkernel.mapper.FinanceSharedDtoMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Maps between shared-kernel finance DTOs (cross-module contract) and finance module REST DTOs.
 */
@Component
public class FinanceModuleDtoMapper {

    private final ModelMapper modelMapper;

    public FinanceModuleDtoMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public com.xdev.ooms.finance.financialtransaction.dto.FinancialTransactionDto fromShared(
            com.xdev.ooms.sharedkernel.communicator.models.shared.FinancialTransactionDto source) {
        if (source == null) {
            return null;
        }
        com.xdev.ooms.finance.financialtransaction.dto.FinancialTransactionDto target =
                new com.xdev.ooms.finance.financialtransaction.dto.FinancialTransactionDto();
        copySharedBaseToModule(source, target);
        target.setTransactionType(source.getTransactionType());
        target.setDirection(source.getDirection());
        target.setAmount(source.getAmount());
        target.setCurrency(source.getCurrency());
        target.setPaymentMethod(source.getPaymentMethod());
        target.setCheckNumber(source.getCheckNumber());
        target.setLotNumber(source.getLotNumber());
        target.setDescription(source.getDescription());
        target.setInvoiceReference(source.getInvoiceReference());
        target.setReceiptReference(source.getReceiptReference());
        target.setTransactionDate(source.getTransactionDate());
        target.setApproved(source.getApproved());
        target.setApprovalDate(source.getApprovalDate());
        target.setApprovedBy(source.getApprovedBy());
        target.setExternalTransactionId(source.getExternalTransactionId());
        target.setPaidAmount(source.getPaidAmount());
        target.setUnpaidAmount(source.getUnpaidAmount());
        target.setOperationType(source.getOperationType());
        target.setResourceName(source.getResourceName());
        target.setVendorName(source.getVendorName());
        target.setSyncProductionState(
                source.getSyncProductionState() != null ? source.getSyncProductionState() : Boolean.TRUE);
        target.setSupplier(FinanceSharedDtoMapper.copySupplier(source.getsupplier()));
        target.setBankAccount(FinanceSharedDtoMapper.copyBankAccount(source.getBankAccount()));
        target.setExpense(FinanceSharedDtoMapper.copyExpense(source.getExpense()));
        target.setCreatedBy(source.getCreatedBy());
        target.setCreatedDate(source.getCreatedDate());
        target.setLastModifiedBy(source.getLastModifiedBy());
        target.setLastModifiedDate(source.getLastModifiedDate());
        target.setTenantId(source.getTenantId());
        return target;
    }

    public com.xdev.ooms.sharedkernel.communicator.models.shared.FinancialTransactionDto toShared(
            com.xdev.ooms.finance.financialtransaction.dto.FinancialTransactionDto source) {
        if (source == null) {
            return null;
        }
        com.xdev.ooms.sharedkernel.communicator.models.shared.FinancialTransactionDto target =
                new com.xdev.ooms.sharedkernel.communicator.models.shared.FinancialTransactionDto();
        copyModuleBaseToShared(source, target);
        target.setTransactionType(source.getTransactionType());
        target.setDirection(source.getDirection());
        target.setAmount(source.getAmount());
        target.setCurrency(source.getCurrency());
        target.setPaymentMethod(source.getPaymentMethod());
        target.setCheckNumber(source.getCheckNumber());
        target.setLotNumber(source.getLotNumber());
        target.setDescription(source.getDescription());
        target.setInvoiceReference(source.getInvoiceReference());
        target.setReceiptReference(source.getReceiptReference());
        target.setTransactionDate(source.getTransactionDate());
        target.setApproved(source.getApproved());
        target.setApprovalDate(source.getApprovalDate());
        target.setApprovedBy(source.getApprovedBy());
        target.setExternalTransactionId(source.getExternalTransactionId());
        target.setPaidAmount(source.getPaidAmount());
        target.setUnpaidAmount(source.getUnpaidAmount());
        target.setOperationType(source.getOperationType());
        target.setResourceName(source.getResourceName());
        target.setVendorName(source.getVendorName());
        target.setSyncProductionState(source.getSyncProductionState());
        target.setsupplier(FinanceSharedDtoMapper.copySupplier(source.getSupplier()));
        target.setBankAccount(FinanceSharedDtoMapper.copyBankAccount(source.getBankAccount()));
        target.setExpense(FinanceSharedDtoMapper.copyExpense(source.getExpense()));
        target.setCreatedBy(source.getCreatedBy());
        target.setCreatedDate(source.getCreatedDate());
        target.setLastModifiedBy(source.getLastModifiedBy());
        target.setLastModifiedDate(source.getLastModifiedDate());
        target.setTenantId(source.getTenantId());
        return target;
    }

    public BankAccountDto toSharedBankAccount(BankAccount entity) {
        return modelMapper.map(entity, BankAccountDto.class);
    }

    public com.xdev.ooms.sharedkernel.communicator.models.shared.ExpenseDto toSharedExpenseReference(Expense entity) {
        if (entity == null) {
            return null;
        }
        com.xdev.ooms.sharedkernel.communicator.models.shared.ExpenseDto target =
                new com.xdev.ooms.sharedkernel.communicator.models.shared.ExpenseDto();
        target.setId(entity.getId());
        target.setExternalId(entity.getExternalId());
        return target;
    }

    public com.xdev.ooms.sharedkernel.communicator.models.shared.ExpenseDto toSharedExpense(ExpenseDto source) {
        if (source == null) {
            return null;
        }
        com.xdev.ooms.sharedkernel.communicator.models.shared.ExpenseDto target =
                new com.xdev.ooms.sharedkernel.communicator.models.shared.ExpenseDto();
        copyModuleBaseToShared(source, target);
        target.setInvoiceRef(source.getInvoiceRef());
        target.setPurchaseNature(source.getPurchaseNature());
        target.setObject(source.getObject());
        target.setDate(source.getDate());
        target.setAmount(source.getAmount());
        target.setVendor(source.getVendor());
        target.setCategory(source.getCategory() != null ? source.getCategory().name() : null);
        target.setPaymentMethod(source.getPaymentMethod() != null ? source.getPaymentMethod().name() : null);
        target.setStatus(source.getStatus() != null ? source.getStatus().name() : null);
        target.setNotes(source.getNotes());
        target.setReceiptNumber(source.getReceiptNumber());
        target.setCreatedBy(source.getCreatedBy());
        target.setApproved(source.getApproved());
        target.setApprovalDate(source.getApprovalDate());
        target.setCheckNumber(source.getCheckNumber());
        return target;
    }

    private void copySharedBaseToModule(
            BaseDto source,
            com.xdev.ooms.sharedkernel.dtos.BaseDto<?> target) {
        target.setId(source.getId());
        target.setExternalId(source.getExternalId());
        target.setDeleted(source.getDeleted());
    }

    private void copyModuleBaseToShared(
            com.xdev.ooms.sharedkernel.dtos.BaseDto<?> source,
            BaseDto target) {
        target.setId(source.getId());
        target.setExternalId(source.getExternalId());
        target.setDeleted(source.getDeleted());
    }
}
