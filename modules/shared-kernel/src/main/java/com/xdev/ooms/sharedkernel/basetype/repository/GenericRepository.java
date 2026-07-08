package com.xdev.ooms.sharedkernel.basetype.repository;

import com.xdev.ooms.sharedkernel.Enum.TypeCategory;
import com.xdev.ooms.sharedkernel.basetype.entity.BaseType;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GenericRepository extends BaseRepository<BaseType> {
    List<BaseType> findAllByType(TypeCategory type);

    Optional<BaseType> findFirstByTypeAndNameIgnoreCaseAndIsDeletedFalse(TypeCategory type, String name);

    Optional<BaseType> findFirstByTypeAndNameIgnoreCaseAndIdNotAndIsDeletedFalse(TypeCategory type, String name, UUID id);
}
