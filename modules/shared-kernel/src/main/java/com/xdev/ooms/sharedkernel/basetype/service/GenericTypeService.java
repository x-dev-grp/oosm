package com.xdev.ooms.sharedkernel.basetype.service;

import com.xdev.ooms.sharedkernel.Enum.TypeCategory;
import com.xdev.ooms.sharedkernel.basetype.dto.BaseTypeDto;
import com.xdev.ooms.sharedkernel.basetype.entity.BaseType;
import com.xdev.ooms.sharedkernel.basetype.repository.GenericRepository;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class GenericTypeService extends BaseServiceImpl<BaseType, BaseTypeDto, BaseTypeDto> {
    private final GenericRepository repository;

    public GenericTypeService(GenericRepository repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
        this.repository = repository;
    }

    public BaseType handleBaseType(BaseTypeDto dto) {
        if (dto == null) {
            return null;
        }
        if (dto.getId() != null) {
            BaseType existing = repository.findById(dto.getId()).orElse(null);
            if (existing != null) {
                return existing;
            }
        }
        BaseType baseType = modelMapper.map(dto, BaseType.class);
        baseType.setId(null);
        return repository.save(baseType);
    }

    public BaseType handelBaseType(BaseTypeDto dto) {
        return handleBaseType(dto);
    }

    public List<BaseType> getAllTypes(TypeCategory type) {
        return repository.findAllByType(type);
    }

    @Override
    public Set<Action> actionsMapping(BaseType baseType) {
        return Set.of(Action.UPDATE, Action.DELETE, Action.READ);
    }
}
