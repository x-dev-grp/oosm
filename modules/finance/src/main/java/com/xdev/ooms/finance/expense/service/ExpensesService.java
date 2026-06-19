package com.xdev.ooms.finance.expense.service;


import com.xdev.ooms.finance.expense.dto.ExpenseDto;
import com.xdev.ooms.finance.financialtransaction.dto.FinancialTransactionDto;
import com.xdev.ooms.finance.internal.FinanceModuleDtoMapper;
import com.xdev.ooms.finance.expense.entity.Expense;
import com.xdev.ooms.finance.financialtransaction.entity.FinancialTransaction;
import com.xdev.ooms.finance.financialtransaction.repository.FinancialTransactionRepository;
import  com.xdev.ooms.sharedkernel.Enum.Currency;
import  com.xdev.ooms.sharedkernel.Enum.TransactionDirection;
import  com.xdev.ooms.sharedkernel.Enum.TransactionType;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.ports.InvoiceNumberPort;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Service
public class ExpensesService extends BaseServiceImpl<Expense, ExpenseDto, ExpenseDto> {
    private final FinancialTransactionRepository financialTransactionRepository;
    private final FinanceModuleDtoMapper moduleDtoMapper;
    private final InvoiceNumberPort invoiceNumberPort;

    public ExpensesService(
            BaseRepository<Expense> repository,
            ModelMapper modelMapper,
            FinancialTransactionRepository financialTransactionRepository,
            FinanceModuleDtoMapper moduleDtoMapper,
            InvoiceNumberPort invoiceNumberPort) {
        super(repository, modelMapper);
        this.financialTransactionRepository = financialTransactionRepository;
        this.moduleDtoMapper = moduleDtoMapper;
        this.invoiceNumberPort = invoiceNumberPort;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExpenseDto save(ExpenseDto request) {
        if (request.getInvoiceRef() == null || request.getInvoiceRef().isBlank()) {
            request.setInvoiceRef(invoiceNumberPort.nextInvoiceNumber());
        }
        Expense expense = modelMapper.map(request, Expense.class);
        Expense savdExpense = repository.save(expense);
        FinancialTransactionDto financialTransactionDto = new FinancialTransactionDto();
        financialTransactionDto.setTransactionType(TransactionType.EXPENSE);
        financialTransactionDto.setDirection(TransactionDirection.OUTBOUND);
        financialTransactionDto.setAmount(BigDecimal.valueOf(savdExpense.getAmount()));
        financialTransactionDto.setCurrency(Currency.TND);
        financialTransactionDto.setExpense(moduleDtoMapper.toSharedExpenseReference(savdExpense));
        financialTransactionDto.setCheckNumber(savdExpense.getCheckNumber() != null ? savdExpense.getCheckNumber() : null);
        financialTransactionDto.setInvoiceReference(savdExpense.getInvoiceRef());
        financialTransactionDto.setTransactionDate(LocalDateTime.now());
        financialTransactionDto.setApproved(true);
        financialTransactionDto.setApprovalDate(LocalDateTime.now());
        FinancialTransaction financialTransaction = modelMapper.map(financialTransactionDto, FinancialTransaction.class);
        financialTransactionRepository.save(financialTransaction);
        return modelMapper.map(savdExpense, ExpenseDto.class);
    }

    @Override
    public Set<Action> actionsMapping(Expense expense) {
        return new HashSet<>(Set.of(Action.UPDATE, Action.DELETE, Action.READ));
    }
}
