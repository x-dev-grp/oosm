package com.xdev.ooms.finance.repo;

import com.xdev.ooms.finance.model.BankAccount;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankAccountRepository extends BaseRepository<BankAccount> {
}