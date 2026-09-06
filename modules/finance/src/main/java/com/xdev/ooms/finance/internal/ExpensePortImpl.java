package com.xdev.ooms.finance.internal;

import com.xdev.ooms.finance.expense.dto.ExpenseDto;
import com.xdev.ooms.finance.expense.repository.ExpensesRepository;
import com.xdev.ooms.finance.expense.service.ExpensesService;
import com.xdev.ooms.sharedkernel.Enum.ExpenseStatus;
import com.xdev.ooms.sharedkernel.ports.ExpensePort;
import com.xdev.ooms.sharedkernel.ports.ExpenseRecordCommand;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ExpensePortImpl implements ExpensePort {

    private final ExpensesService expensesService;
    private final ExpensesRepository expensesRepository;

    public ExpensePortImpl(ExpensesService expensesService, ExpensesRepository expensesRepository) {
        this.expensesService = expensesService;
        this.expensesRepository = expensesRepository;
    }

    @Override
    public String record(ExpenseRecordCommand command) {
        ExpenseDto dto = new ExpenseDto();
        dto.setVendor(command.vendor());
        dto.setAmount(command.amount());
        dto.setCategory(command.category());
        dto.setPaymentMethod(command.paymentMethod());
        dto.setObject(command.object());
        dto.setPurchaseNature(command.purchaseNature());
        dto.setNotes(appendExternalReference(command.notes(), command.externalReference()));
        LocalDate date = command.date() != null ? command.date() : LocalDate.now();
        dto.setDate(date);
        dto.setStatus(ExpenseStatus.PAID);
        dto.setApproved(true);
        dto.setApprovalDate(date);
        if (command.externalReference() != null && !command.externalReference().isBlank()) {
            dto.setInvoiceRef("IMP-" + command.externalReference().trim());
        }
        ExpenseDto saved = expensesService.save(dto);
        return saved.getInvoiceRef();
    }

    @Override
    public boolean existsByExternalReference(String externalReference) {
        if (externalReference == null || externalReference.isBlank()) {
            return false;
        }
        String token = "ExternalRef:" + externalReference.trim();
        return expensesRepository.existsByNotesContainingIgnoreCaseAndIsDeletedFalse(token)
                || expensesRepository.existsByInvoiceRefIgnoreCaseAndIsDeletedFalse("IMP-" + externalReference.trim());
    }

    private String appendExternalReference(String notes, String externalReference) {
        if (externalReference == null || externalReference.isBlank()) {
            return notes;
        }
        String prefix = "ExternalRef:" + externalReference.trim();
        if (notes == null || notes.isBlank()) {
            return prefix;
        }
        return prefix + " | " + notes.trim();
    }
}
