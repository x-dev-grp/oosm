package com.xdev.ooms.conditioning.support;

import com.xdev.ooms.conditioning.expedition.dto.GenealogyDto;
import com.xdev.ooms.production.model.FiltrationOperation;
import com.xdev.ooms.production.model.StorageUnit;
import com.xdev.ooms.production.repository.FiltrationOperationRepo;
import com.xdev.ooms.production.repository.StorageUnitRepo;
import com.xdev.ooms.production.service.GenealogyService;
import com.xdev.ooms.sharedkernel.communicator.models.shared.StorageUnitDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
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
        com.xdev.ooms.production.dto.GenealogyDto genealogy =
                genealogyService.getFullGenealogy(storageUnitId);
        return modelMapper.map(genealogy, GenealogyDto.class);
    }

    public Object getFiltrationOperation(UUID id) {
        FiltrationOperation operation = filtrationOperationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Operation de filtration introuvable: " + id));
        return modelMapper.map(operation, Object.class);
    }
}
