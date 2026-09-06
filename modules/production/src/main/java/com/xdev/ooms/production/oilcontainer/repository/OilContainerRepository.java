package com.xdev.ooms.production.oilcontainer.repository;

import com.xdev.ooms.production.oilcontainer.entity.OilContainer;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OilContainerRepository extends BaseRepository<OilContainer> {
    Optional<OilContainer> findFirstByNameIgnoreCaseAndIsDeletedFalse(String name);
}
