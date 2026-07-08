package com.xdev.ooms.sharedkernel.basetype.service;

import com.xdev.ooms.sharedkernel.basetype.defaults.TunisiaBaseTypeDefaults;
import com.xdev.ooms.sharedkernel.basetype.entity.BaseType;
import com.xdev.ooms.sharedkernel.basetype.repository.GenericRepository;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BaseTypeProvisioningService {

    private final GenericRepository repository;

    public BaseTypeProvisioningService(GenericRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public int provisionTunisiaDefaults(UUID tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID is required to provision base types");
        }

        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "provisionTunisiaDefaults", tenantId);

        List<BaseType> toSave = new ArrayList<>();
        for (TunisiaBaseTypeDefaults.BaseTypeTemplate template : TunisiaBaseTypeDefaults.all()) {
            if (repository.findFirstByTypeAndNameIgnoreCaseAndIsDeletedFalse(template.type(), template.name()).isPresent()) {
                continue;
            }

            BaseType baseType = new BaseType();
            baseType.setTenantId(tenantId);
            baseType.setType(template.type());
            baseType.setName(template.name());
            baseType.setDescription(template.description());
            baseType.setDeleted(false);
            toSave.add(baseType);
        }

        if (!toSave.isEmpty()) {
            repository.saveAll(toSave);
        }

        OOSMLogger.logMethodExit(this.getClass(), "provisionTunisiaDefaults", toSave.size());
        OOSMLogger.logPerformance(this.getClass(), "provisionTunisiaDefaults", startTime, System.currentTimeMillis());
        return toSave.size();
    }
}
