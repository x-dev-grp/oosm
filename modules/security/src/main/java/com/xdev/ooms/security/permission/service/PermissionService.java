package com.xdev.ooms.security.permission.service;

import com.xdev.ooms.security.permission.repository.PermissionRepository;
import com.xdev.ooms.security.permission.dto.PermissionDTO;
import com.xdev.ooms.security.permission.entity.Permission;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.utils.OSMLogger;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PermissionService extends BaseServiceImpl<Permission, PermissionDTO, PermissionDTO> {
    private final PermissionRepository permissionRepository;

    protected PermissionService(BaseRepository<Permission> repository, ModelMapper modelMapper, PermissionRepository permissionRepository) {
        super(repository, modelMapper);
        
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "PermissionService", "Initializing PermissionService");
        
        try {
            this.permissionRepository = permissionRepository;
            
            OSMLogger.logMethodExit(this.getClass(), "PermissionService", "PermissionService initialized successfully");
            OSMLogger.logPerformance(this.getClass(), "PermissionService", startTime, System.currentTimeMillis());
            OSMLogger.logSecurityEvent(this.getClass(), "PERMISSION_SERVICE_INITIALIZED", 
                "Permission service initialized successfully");
            
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error initializing PermissionService", e);
            throw e;
        }
    }

    @Override
    public List<PermissionDTO> findAll() {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "findAll");

        try {
            List<Permission> data = repository.findAllByIsDeletedFalse();
            List<PermissionDTO> result = data.stream().map(item -> modelMapper.map(item, outDTOClass)).toList();
            OSMLogger.logMethodExit(this.getClass(), "findAll", "Found " + result.size() + " entities");
            OSMLogger.logPerformance(this.getClass(), "findAll", startTime, System.currentTimeMillis());
            OSMLogger.logDataAccess(this.getClass(), "READ_ALL", entityClass.getSimpleName());
            return result;
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error finding all entities", e);
            throw e;
        }
    }
}
