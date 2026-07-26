package com.xdev.ooms.hr.legal.repository;

import com.xdev.ooms.hr.legal.entity.CompanyHrLegalProfile;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyHrLegalProfileRepository extends BaseRepository<CompanyHrLegalProfile> {

    Optional<CompanyHrLegalProfile> findFirstByTenantIdAndIsDeletedFalseOrderByCreatedDateDesc(UUID tenantId);
}
