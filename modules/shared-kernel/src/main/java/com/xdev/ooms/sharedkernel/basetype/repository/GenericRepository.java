package com.xdev.ooms.sharedkernel.basetype.repository;

import com.xdev.ooms.sharedkernel.Enum.TypeCategory;
import com.xdev.ooms.sharedkernel.basetype.entity.BaseType;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GenericRepository extends BaseRepository<BaseType> {
    List<BaseType> findAllByType(TypeCategory type);
}
