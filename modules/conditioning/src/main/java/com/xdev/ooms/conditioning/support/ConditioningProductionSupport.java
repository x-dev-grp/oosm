package com.xdev.ooms.conditioning.support;

import com.xdev.ooms.conditioning.expedition.dto.GenealogyDto;
import com.xdev.ooms.production.filtration.entity.FiltrationOperation;
import com.xdev.ooms.production.filtration.repository.FiltrationOperationRepo;
import com.xdev.ooms.production.genealogy.service.GenealogyService;
import com.xdev.ooms.production.storageunit.entity.StorageUnit;
import com.xdev.ooms.production.storageunit.repository.StorageUnitRepo;
import com.xdev.ooms.sharedkernel.communicator.models.shared.StorageUnitDto;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ConditioningProductionSupport {

    private final StorageUnitRepo storageUnitRepo;
    private final FiltrationOperationRepo filtrationOperationRepo;
    private final GenealogyService genealogyService;
    private final ModelMapper modelMapper;

    public StorageUnitDto getStorageUnit(UUID id) {
        StorageUnit storageUnit = storageUnitRepo.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Cuve d'huile introuvable (ID: " + id + ")"));
        return modelMapper.map(storageUnit, StorageUnitDto.class);
    }

    public GenealogyDto getGenealogy(UUID storageUnitId) {
        com.xdev.ooms.production.genealogy.dto.GenealogyDto genealogy =
                genealogyService.getFullGenealogy(storageUnitId);
        return modelMapper.map(genealogy, GenealogyDto.class);
    }

    public Object getFiltrationOperation(UUID id) {
        FiltrationOperation operation = filtrationOperationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Operation de filtration introuvable: " + id));
        return modelMapper.map(operation, Object.class);
    }

    public ConditioningProductionSupport(StorageUnitRepo storageUnitRepo, FiltrationOperationRepo filtrationOperationRepo, GenealogyService genealogyService, ModelMapper modelMapper) {
        this.storageUnitRepo = storageUnitRepo;
        this.filtrationOperationRepo = filtrationOperationRepo;
        this.genealogyService = genealogyService;
        this.modelMapper = modelMapper;
    }
}
