package com.xdev.ooms.sharedkernel.mapper;

import com.xdev.ooms.sharedkernel.communicator.models.common.dtos.BaseDto;
import com.xdev.ooms.sharedkernel.communicator.models.shared.BankAccountDto;
import com.xdev.ooms.sharedkernel.communicator.models.shared.ExpenseDto;
import com.xdev.ooms.sharedkernel.communicator.models.shared.FinancialTransactionDto;
import com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto;

/**
 * Canonical copy utilities for finance-related shared-kernel DTOs.
 * Cross-module contracts should use these types; module DTOs map via {@code FinanceModuleDtoMapper}.
 */
public final class FinanceSharedDtoMapper {

    private FinanceSharedDtoMapper() {
    }

    public static void copyBaseFields(BaseDto source, BaseDto target) {
        if (source == null || target == null) {
            return;
        }
        target.setId(source.getId());
        target.setDeleted(source.getDeleted());
        target.setActions(source.getActions());
    }

    public static FinancialTransactionDto copyFinancialTransaction(FinancialTransactionDto source) {
        if (source == null) {
            return null;
        }
        FinancialTransactionDto target = new FinancialTransactionDto();
        copyFinancialTransactionFields(source, target);
        return target;
    }

    public static void copyFinancialTransactionFields(FinancialTransactionDto source, FinancialTransactionDto target) {
        if (source == null || target == null) {
            return;
        }
        copyBaseFields(source, target);
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
        target.setTenantId(source.getTenantId());
        target.setCreatedBy(source.getCreatedBy());
        target.setCreatedDate(source.getCreatedDate());
        target.setLastModifiedBy(source.getLastModifiedBy());
        target.setLastModifiedDate(source.getLastModifiedDate());
        target.setOperationType(source.getOperationType());
        target.setResourceName(source.getResourceName());
        target.setVendorName(source.getVendorName());
        target.setSyncProductionState(source.getSyncProductionState());
        target.setsupplier(copySupplier(source.getsupplier()));
        target.setBankAccount(copyBankAccount(source.getBankAccount()));
        target.setExpense(copyExpense(source.getExpense()));
    }

    public static BankAccountDto copyBankAccount(BankAccountDto source) {
        if (source == null) {
            return null;
        }
        BankAccountDto target = new BankAccountDto();
        copyBankAccountFields(source, target);
        return target;
    }

    public static void copyBankAccountFields(BankAccountDto source, BankAccountDto target) {
        if (source == null || target == null) {
            return;
        }
        copyBaseFields(source, target);
        target.setBankName(source.getBankName());
        target.setRib(source.getRib());
        target.setIban(source.getIban());
        target.setBicSwift(source.getBicSwift());
        target.setBankBranch(source.getBankBranch());
        target.setAccountType(source.getAccountType());
        target.setActive(source.getActive());
        target.setCurrency(source.getCurrency());
    }

    public static ExpenseDto copyExpense(ExpenseDto source) {
        if (source == null) {
            return null;
        }
        ExpenseDto target = new ExpenseDto();
        copyExpenseFields(source, target);
        return target;
    }

    public static void copyExpenseFields(ExpenseDto source, ExpenseDto target) {
        if (source == null || target == null) {
            return;
        }
        copyBaseFields(source, target);
        target.setInvoiceRef(source.getInvoiceRef());
        target.setPurchaseNature(source.getPurchaseNature());
        target.setObject(source.getObject());
        target.setDate(source.getDate());
        target.setAmount(source.getAmount());
        target.setVendor(source.getVendor());
        target.setCategory(source.getCategory());
        target.setPaymentMethod(source.getPaymentMethod());
        target.setStatus(source.getStatus());
        target.setNotes(source.getNotes());
        target.setReceiptNumber(source.getReceiptNumber());
        target.setCreatedBy(source.getCreatedBy());
        target.setApproved(source.getApproved());
        target.setApprovalDate(source.getApprovalDate());
        target.setCheckNumber(source.getCheckNumber());
    }

    public static SupplierDto copySupplier(SupplierDto source) {
        if (source == null) {
            return null;
        }
        SupplierDto target = new SupplierDto();
        copyBaseFields(source, target);
        target.setGenericSupplierType(source.getGenericSupplierType());
        target.setHasStorage(source.getHasStorage());
        target.setName(source.getName());
        target.setLastname(source.getLastname());
        target.setPhone(source.getPhone());
        target.setEmail(source.getEmail());
        target.setAddress(source.getAddress());
        target.setRegion(source.getRegion());
        target.setRib(source.getRib());
        target.setBankName(source.getBankName());
        target.setMatriculeFiscal(source.getMatriculeFiscal());
        target.setStorageUnit(source.getStorageUnit());
        return target;
    }
}
