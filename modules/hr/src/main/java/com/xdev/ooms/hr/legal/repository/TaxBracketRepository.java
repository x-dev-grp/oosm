package com.xdev.ooms.hr.legal.repository;

import com.xdev.ooms.hr.legal.entity.TaxBracket;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaxBracketRepository extends BaseRepository<TaxBracket> {

    List<TaxBracket> findByTaxConfiguration_IdAndIsDeletedFalseOrderBySortOrderAsc(UUID taxConfigurationId);
}
