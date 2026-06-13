package com.xdev.ooms.finance.repo;

import com.xdev.ooms.finance.model.BaseType;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GenericRepository extends BaseRepository<BaseType> {
    Optional<BaseType> findByExternalId(UUID externalId);
}
