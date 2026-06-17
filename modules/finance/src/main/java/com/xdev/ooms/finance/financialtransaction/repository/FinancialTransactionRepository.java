package com.xdev.ooms.finance.financialtransaction.repository;

import com.xdev.ooms.finance.financialtransaction.entity.FinancialTransaction;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FinancialTransactionRepository extends BaseRepository<FinancialTransaction> {

    List<FinancialTransaction> findBySupplier_IdAndIsDeletedFalseOrderByTransactionDateDesc(UUID supplierId);
} 