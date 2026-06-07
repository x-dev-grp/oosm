package com.xdev.ooms.finance.repo;

import com.xdev.ooms.finance.model.Expense;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpensesRepository extends BaseRepository<Expense> {
}