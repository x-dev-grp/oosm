package com.xdev.ooms.hr.loan.repository;

import com.xdev.ooms.hr.loan.entity.LoanInstallment;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoanInstallmentRepository extends BaseRepository<LoanInstallment> {
    List<LoanInstallment> findByLoan_IdAndIsDeletedFalseOrderByDueDateAsc(UUID loanId);
}
