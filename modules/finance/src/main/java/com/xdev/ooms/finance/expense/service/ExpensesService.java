package com.xdev.ooms.finance.expense.service;


import com.xdev.ooms.finance.expense.dto.ExpenseDto;
import com.xdev.ooms.finance.financialtransaction.dto.FinancialTransactionDto;
import com.xdev.ooms.finance.financialtransaction.service.FinancialTransactionService;
import com.xdev.ooms.finance.internal.FinanceModuleDtoMapper;
import com.xdev.ooms.finance.expense.entity.Expense;
import com.xdev.ooms.sharedkernel.Enum.Currency;
import com.xdev.ooms.sharedkernel.Enum.PaymentMethod;
import com.xdev.ooms.sharedkernel.Enum.ResourceName;
import com.xdev.ooms.sharedkernel.Enum.TransactionDirection;
import com.xdev.ooms.sharedkernel.Enum.TransactionType;
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
    private final FinancialTransactionService financialTransactionService;
    private final FinanceModuleDtoMapper moduleDtoMapper;
    private final InvoiceNumberPort invoiceNumberPort;

    public ExpensesService(
            BaseRepository<Expense> repository,
            ModelMapper modelMapper,
            FinancialTransactionService financialTransactionService,
            FinanceModuleDtoMapper moduleDtoMapper,
            InvoiceNumberPort invoiceNumberPort) {
        super(repository, modelMapper);
        this.financialTransactionService = financialTransactionService;
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
        Expense savedExpense = repository.save(expense);
        savedExpense = ensureQrCodeIfSupported(savedExpense);

        FinancialTransactionDto financialTransactionDto = new FinancialTransactionDto();
        financialTransactionDto.setTransactionType(TransactionType.EXPENSE);
        financialTransactionDto.setDirection(TransactionDirection.OUTBOUND);
        financialTransactionDto.setAmount(BigDecimal.valueOf(savedExpense.getAmount()));
        financialTransactionDto.setCurrency(Currency.TND);
        financialTransactionDto.setPaymentMethod(
                savedExpense.getPaymentMethod() != null ? savedExpense.getPaymentMethod() : PaymentMethod.CASH);
        financialTransactionDto.setExpense(moduleDtoMapper.toSharedExpenseReference(savedExpense));
        financialTransactionDto.setCheckNumber(savedExpense.getCheckNumber());
        financialTransactionDto.setInvoiceReference(savedExpense.getInvoiceRef());
        financialTransactionDto.setTransactionDate(LocalDateTime.now());
        financialTransactionDto.setApproved(true);
        financialTransactionDto.setApprovalDate(LocalDateTime.now());
        financialTransactionDto.setResourceName(ResourceName.Expense);
        financialTransactionDto.setExternalTransactionId(savedExpense.getId().toString());
        financialTransactionDto.setDescription(buildExpenseDescription(savedExpense));
        // Sync marks the expense PAID via FinancialTransactionService post-save.
        financialTransactionDto.setSyncProductionState(true);
        financialTransactionService.save(financialTransactionDto);

        return modelMapper.map(savedExpense, ExpenseDto.class);
    }

    private String buildExpenseDescription(Expense expense) {
        if (expense.getObject() != null && !expense.getObject().isBlank()) {
            return expense.getObject().trim();
        }
        if (expense.getNotes() != null && !expense.getNotes().isBlank()) {
            return expense.getNotes().trim();
        }
        if (expense.getPurchaseNature() != null && !expense.getPurchaseNature().isBlank()) {
            return expense.getPurchaseNature().trim();
        }
        return "Expense " + expense.getInvoiceRef();
    }

    @Override
    public Set<Action> actionsMapping(Expense expense) {
        return new HashSet<>(Set.of(Action.UPDATE, Action.DELETE, Action.READ));
    }

    @Override
    protected String getEntityType() {
        return "EXPENSE";
    }

    @Override
    protected String getLabel(Expense entity) {
        if (entity == null) {
            return "Expense";
        }
        if (entity.getObject() != null && !entity.getObject().isBlank()) {
            return entity.getObject();
        }
        if (entity.getInvoiceRef() != null && !entity.getInvoiceRef().isBlank()) {
            return entity.getInvoiceRef();
        }
        return entity.getId() != null ? "Expense " + entity.getId() : "Expense";
    }

    @Override
    protected String getStatus(Expense entity) {
        if (entity == null || entity.getStatus() == null) {
            return "UNKNOWN";
        }
        return entity.getStatus().name();
    }

    @Override
    protected String getMobileRoute() {
        return "/finance/expenses";
    }

    @Override
    protected String getWebRoute(Expense entity) {
        if (entity == null || entity.getId() == null) {
            return "/finance/expenses";
        }
        return "/finance/expenses/" + entity.getId() + "/view";
    }
}
