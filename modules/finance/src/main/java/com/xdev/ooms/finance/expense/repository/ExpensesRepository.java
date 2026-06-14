package com.xdev.ooms.finance.expense.repository;

import com.xdev.ooms.finance.expense.entity.Expense;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpensesRepository extends BaseRepository<Expense> {
}