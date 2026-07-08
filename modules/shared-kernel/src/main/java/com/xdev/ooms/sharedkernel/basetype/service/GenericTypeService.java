package com.xdev.ooms.sharedkernel.basetype.service;

import com.xdev.ooms.sharedkernel.Enum.TypeCategory;
import com.xdev.ooms.sharedkernel.basetype.dto.BaseTypeDto;
import com.xdev.ooms.sharedkernel.basetype.entity.BaseType;
import com.xdev.ooms.sharedkernel.basetype.repository.GenericRepository;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

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

    @Override
    @Transactional
    public BaseTypeDto save(BaseTypeDto request) {
        if (request == null) {
            return null;
        }

        String name = normalizeName(request.getName());
        if (name.isBlank() || request.getType() == null) {
            return super.save(request);
        }

        return repository.findFirstByTypeAndNameIgnoreCaseAndIsDeletedFalse(request.getType(), name)
                .map(existing -> modelMapper.map(existing, BaseTypeDto.class))
                .orElseGet(() -> {
                    request.setName(name);
                    return super.save(request);
                });
    }

    @Override
    @Transactional
    public BaseTypeDto update(BaseTypeDto request) {
        if (request == null || request.getId() == null) {
            return super.update(request);
        }

        String name = normalizeName(request.getName());
        if (!name.isBlank() && request.getType() != null) {
            UUID id = request.getId();
            repository.findFirstByTypeAndNameIgnoreCaseAndIdNotAndIsDeletedFalse(request.getType(), name, id)
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("A type with the same name and category already exists.");
                    });
            request.setName(name);
        }

        return super.update(request);
    }

    public List<BaseType> getAllTypes(TypeCategory type) {
        return repository.findAllByType(type);
    }

    @Override
    public Set<Action> actionsMapping(BaseType baseType) {
        return Set.of(Action.UPDATE, Action.DELETE, Action.READ);
    }

    private String normalizeName(String name) {
        return name == null ? "" : name.trim().replaceAll("\\s+", " ");
    }
}
