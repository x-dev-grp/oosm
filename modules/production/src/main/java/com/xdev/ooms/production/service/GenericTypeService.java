package com.xdev.ooms.production.service;


import com.xdev.ooms.production.dto.*;
import  com.xdev.ooms.sharedkernel.Enum.TypeCategory;
import com.xdev.ooms.production.model.BaseType;
import com.xdev.ooms.production.repository.GenericRepository;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.utils.OSMLogger;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
public class GenericTypeService extends BaseServiceImpl<BaseType, BaseTypeDto, BaseTypeDto> {
    private final GenericRepository genericRepository;

    public GenericTypeService(BaseRepository<BaseType> repository, ModelMapper modelMapper, GenericRepository genericRepository) {
        super(repository, modelMapper);
        this.genericRepository = genericRepository;
    }

    public BaseType handelBaseType(BaseTypeDto dto) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "handelBaseType", dto);

        try {
            if (dto == null) {
                OSMLogger.logDataAccess(this.getClass(), "BASE_TYPE_HANDLE", "BaseType");
                return null;
            }

            if (dto.getExternalId() != null) {
                OSMLogger.logDataAccess(this.getClass(), "BASE_TYPE_LOOKUP", "BaseType");
                BaseType existingBaseType = genericRepository.findByExternalId(dto.getExternalId()).orElse(null);
                if (existingBaseType != null) {
                    OSMLogger.logDataAccess(this.getClass(), "BASE_TYPE_FOUND", "BaseType");
                    OSMLogger.logMethodExit(this.getClass(), "handelBaseType", existingBaseType);
                    OSMLogger.logPerformance(this.getClass(), "handelBaseType", startTime, System.currentTimeMillis());
                    return existingBaseType;
                }
                OSMLogger.logDataAccess(this.getClass(), "BASE_TYPE_NOT_FOUND", "BaseType");
            }

            OSMLogger.logDataAccess(this.getClass(), "BASE_TYPE_CREATE", "BaseType");
            BaseType baseType = modelMapper.map(dto, BaseType.class);
            baseType.setId(null);
            BaseType savedBaseType = genericRepository.save(baseType);

            OSMLogger.logDataAccess(this.getClass(), "BASE_TYPE_SAVED", "BaseType");
            OSMLogger.logBusinessEvent(this.getClass(), "BASE_TYPE_CREATED",
                    "Created new BaseType: " + savedBaseType.getName() + " (ID: " + savedBaseType.getId() + ")");
            OSMLogger.logMethodExit(this.getClass(), "handelBaseType", savedBaseType);
            OSMLogger.logPerformance(this.getClass(), "handelBaseType", startTime, System.currentTimeMillis());

            return savedBaseType;
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error handling BaseType", e);
            throw e;
        }
    }


    public List<BaseType> getAllTypes(TypeCategory type) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "getAllTypes", type);

        try {
            List<BaseType> types = this.genericRepository.findAllByType(type);

            OSMLogger.logDataAccess(this.getClass(), "GET_ALL_TYPES", "BaseType");
            OSMLogger.logBusinessEvent(this.getClass(), "TYPES_RETRIEVED",
                    "Retrieved " + types.size() + " types for category: " + type.name());
            OSMLogger.logMethodExit(this.getClass(), "getAllTypes", "Found " + types.size() + " types");
            OSMLogger.logPerformance(this.getClass(), "getAllTypes", startTime, System.currentTimeMillis());

            return types;

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error getting all types for category: " + type.name(), e);
            throw e;
        }
    }

    @Override
    public Set<Action> actionsMapping(BaseType baseType) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "actionsMapping", baseType);

        try {
            Set<Action> actions = new HashSet<>();
            actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ));

            OSMLogger.logMethodExit(this.getClass(), "actionsMapping", "Actions: " + actions);
            OSMLogger.logPerformance(this.getClass(), "actionsMapping", startTime, System.currentTimeMillis());

            return actions;

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error mapping actions for BaseType: " + baseType.getId(), e);
            throw e;
        }
    }
}
