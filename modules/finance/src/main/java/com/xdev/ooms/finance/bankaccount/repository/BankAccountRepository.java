package com.xdev.ooms.finance.bankaccount.repository;

import com.xdev.ooms.finance.bankaccount.entity.BankAccount;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankAccountRepository extends BaseRepository<BankAccount> {
}