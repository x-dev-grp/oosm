package com.xdev.ooms.finance.expense.repository;

import com.xdev.ooms.finance.expense.entity.Expense;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpensesRepository extends BaseRepository<Expense> {
    boolean existsByNotesContainingIgnoreCaseAndIsDeletedFalse(String notesFragment);

    boolean existsByInvoiceRefIgnoreCaseAndIsDeletedFalse(String invoiceRef);

    boolean existsByTenantIdAndNotesContainingIgnoreCaseAndIsDeletedFalse(UUID tenantId, String token);
    boolean existsByTenantIdAndInvoiceRefIgnoreCaseAndIsDeletedFalse(UUID tenantId, String ref);
}
