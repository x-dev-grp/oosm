package com.xdev.ooms.finance.internal;

import com.xdev.ooms.finance.expense.dto.ExpenseDto;
import com.xdev.ooms.finance.expense.service.ExpensesService;
import com.xdev.ooms.sharedkernel.Enum.ExpenseStatus;
import com.xdev.ooms.sharedkernel.ports.ExpensePort;
import com.xdev.ooms.sharedkernel.ports.ExpenseRecordCommand;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ExpensePortImpl implements ExpensePort {

    private final ExpensesService expensesService;

    public ExpensePortImpl(ExpensesService expensesService) {
        this.expensesService = expensesService;
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
        dto.setNotes(command.notes());
        dto.setDate(LocalDate.now());
        dto.setStatus(ExpenseStatus.PAID);
        dto.setApproved(true);
        dto.setApprovalDate(LocalDate.now());
        ExpenseDto saved = expensesService.save(dto);
        return saved.getInvoiceRef();
    }
}
