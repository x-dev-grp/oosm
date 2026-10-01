package com.xdev.ooms.production.storageunit.service;

import com.xdev.ooms.production.storageunit.dto.StorageUnitDto;
import com.xdev.ooms.production.storageunit.entity.StorageUnit;
import com.xdev.ooms.production.storageunit.repository.StorageUnitRepo;
import com.xdev.ooms.production.supplier.entity.Supplier;
import com.xdev.ooms.production.supplier.repository.SupplierRepository;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageUnitServiceRulesTest {

    @Mock private BaseRepository<StorageUnit> repository;
    @Mock private StorageUnitRepo storageUnitRepo;
    @Mock private SupplierRepository supplierRepository;

    private final UUID tenantId = UUID.randomUUID();
    private StorageUnitService service;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(tenantId);
        service = new StorageUnitService(repository, new ModelMapper(), storageUnitRepo, supplierRepository);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void updateAppliesFormVolumeButKeepsCostAndSupplier() {
        Supplier supplier = new Supplier();
        StorageUnit tank = tank(tenantId, 500.0);
        tank.setSupplier(supplier);
        when(repository.findByIdAndIsDeletedFalse(tank.getId())).thenReturn(Optional.of(tank));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StorageUnitDto request = new StorageUnitDto();
        request.setId(tank.getId());
        request.setName("Cuve renommee");
        request.setMaxCapacity(1000.0);
        request.setCurrentVolume(450.0);
        request.setAvgCost(0.0);
        request.setTotalCost(0.0);

        service.update(request);

        assertEquals("Cuve renommee", tank.getName());
        assertEquals(450.0, tank.getCurrentVolume());
        assertEquals(4.0, tank.getAvgCost());
        assertEquals(2000.0, tank.getTotalCost());
        assertSame(supplier, tank.getSupplier());
        assertEquals(tenantId, tank.getTenantId());
    }

    @Test
    void updateRefusesCapacityBelowCurrentVolume() {
        StorageUnit tank = tank(tenantId, 500.0);
        when(repository.findByIdAndIsDeletedFalse(tank.getId())).thenReturn(Optional.of(tank));

        StorageUnitDto request = new StorageUnitDto();
        request.setId(tank.getId());
        request.setMaxCapacity(400.0);

        assertThrows(IllegalArgumentException.class, () -> service.update(request));
        verify(repository, never()).save(any());
    }

    @Test
    void updateIgnoresTankOfAnotherTenant() {
        StorageUnit tank = tank(UUID.randomUUID(), 0.0);
        when(repository.findByIdAndIsDeletedFalse(tank.getId())).thenReturn(Optional.of(tank));

        StorageUnitDto request = new StorageUnitDto();
        request.setId(tank.getId());
        request.setName("Intrusion");

        assertNull(service.update(request));
        verify(repository, never()).save(any());
    }

    @Test
    void findByIdHidesTankOfAnotherTenant() {
        StorageUnit tank = tank(UUID.randomUUID(), 0.0);
        when(repository.findByIdAndIsDeletedFalse(tank.getId())).thenReturn(Optional.of(tank));

        assertThrows(EntityNotFoundException.class, () -> service.findById(tank.getId()));
    }

    @Test
    void deleteAllowsTankThatStillHoldsOil() {
        StorageUnit tank = tank(tenantId, 10.0);
        when(repository.findByIdAndIsDeletedFalse(tank.getId())).thenReturn(Optional.of(tank));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.delete(tank.getId());

        assertTrue(tank.getDeleted());
    }

    @Test
    void removeDeletesTankOfCurrentTenant() {
        StorageUnit tank = tank(tenantId, 0.0);
        when(repository.findById(tank.getId())).thenReturn(Optional.of(tank));

        service.remove(tank.getId());

        verify(repository).deleteById(tank.getId());
    }

    @Test
    void removeIgnoresTankOfAnotherTenant() {
        StorageUnit tank = tank(UUID.randomUUID(), 0.0);
        when(repository.findById(tank.getId())).thenReturn(Optional.of(tank));

        service.remove(tank.getId());

        verify(repository, never()).deleteById(any());
    }

    @Test
    void assignSupplierRefusesSupplierOfAnotherTenant() {
        StorageUnit tank = tank(tenantId, 0.0);
        Supplier foreign = new Supplier();
        foreign.setTenantId(UUID.randomUUID());
        UUID supplierId = UUID.randomUUID();
        when(repository.findByIdAndIsDeletedFalse(tank.getId())).thenReturn(Optional.of(tank));
        when(supplierRepository.findByIdAndIsDeletedFalse(supplierId)).thenReturn(Optional.of(foreign));

        assertThrows(EntityNotFoundException.class, () -> service.changeSupplier(tank.getId(), supplierId));
    }

    private StorageUnit tank(UUID owner, double volume) {
        StorageUnit tank = new StorageUnit();
        tank.setId(UUID.randomUUID());
        tank.setTenantId(owner);
        tank.setName("Cuve A");
        tank.setMaxCapacity(1000.0);
        tank.setCurrentVolume(volume);
        tank.setAvgCost(volume > 0 ? 4.0 : 0.0);
        tank.setTotalCost(volume * (volume > 0 ? 4.0 : 0.0));
        return tank;
    }
}
