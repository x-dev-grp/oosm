package com.xdev.ooms.production.filtration.service;

import com.xdev.ooms.production.filtration.dto.FiltrationCompletionDto;
import com.xdev.ooms.production.filtration.dto.FiltrationRequestDto;
import com.xdev.ooms.production.filtration.dto.FiltrationStatus;
import com.xdev.ooms.production.filtration.entity.FiltrationOperation;
import com.xdev.ooms.production.filtration.repository.FiltrationOperationRepo;
import com.xdev.ooms.production.genealogy.service.TraceabilityLotService;
import com.xdev.ooms.production.oiltransaction.service.OilTransactionService;
import com.xdev.ooms.production.storageunit.entity.StorageUnit;
import com.xdev.ooms.production.storageunit.repository.StorageUnitRepo;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.utils.BusinessCodeGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FiltrationServiceRulesTest {

    @Mock private StorageUnitRepo storageUnitRepo;
    @Mock private FiltrationOperationRepo filtrationRepo;
    @Mock private OilTransactionService oilTransactionService;
    @Mock private BusinessCodeGenerator businessCodeGenerator;
    @Mock private TraceabilityLotService traceabilityLotService;
    @Mock private FiltrationDashService filtrationDashService;

    private final UUID tenantId = UUID.randomUUID();
    private FiltrationService service;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(tenantId);
        service = new FiltrationService(storageUnitRepo, filtrationRepo, new ModelMapper(), oilTransactionService,
                businessCodeGenerator, traceabilityLotService, filtrationDashService);
        lenient().when(filtrationRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void createRefusesSameSourceAndTarget() {
        UUID tank = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> service.createFiltration(request(tank, tank, 100.0)));
        verify(filtrationRepo, never()).save(any());
    }

    @Test
    void createRefusesNonPositiveVolume() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createFiltration(request(UUID.randomUUID(), UUID.randomUUID(), 0.0)));
    }

    @Test
    void createRefusesTankOfAnotherTenant() {
        StorageUnit foreign = tank(UUID.randomUUID(), 1000.0, 5.0);
        StorageUnit target = tank(tenantId, 0.0, 0.0);
        when(storageUnitRepo.findByIdAndIsDeletedFalse(foreign.getId())).thenReturn(Optional.of(foreign));

        assertThrows(IllegalArgumentException.class,
                () -> service.createFiltration(request(foreign.getId(), target.getId(), 100.0)));
        verify(filtrationRepo, never()).save(any());
    }

    @Test
    void createRefusesMoreThanSourceHolds() {
        StorageUnit source = tank(tenantId, 50.0, 5.0);
        StorageUnit target = tank(tenantId, 0.0, 0.0);
        when(storageUnitRepo.findByIdAndIsDeletedFalse(source.getId())).thenReturn(Optional.of(source));
        when(storageUnitRepo.findByIdAndIsDeletedFalse(target.getId())).thenReturn(Optional.of(target));

        assertThrows(IllegalArgumentException.class,
                () -> service.createFiltration(request(source.getId(), target.getId(), 100.0)));
    }

    @Test
    void createSavesOperationInCreatedState() {
        StorageUnit source = tank(tenantId, 1000.0, 5.0);
        StorageUnit target = tank(tenantId, 0.0, 0.0);
        when(storageUnitRepo.findByIdAndIsDeletedFalse(source.getId())).thenReturn(Optional.of(source));
        when(storageUnitRepo.findByIdAndIsDeletedFalse(target.getId())).thenReturn(Optional.of(target));

        var result = service.createFiltration(request(source.getId(), target.getId(), 100.0));

        assertEquals(FiltrationStatus.CREATED.name(), result.getStatus());
        assertEquals(1000.0, source.getCurrentVolume());
    }

    @Test
    void completedFiltrationCannotBeDeleted() {
        FiltrationOperation op = operation(FiltrationStatus.COMPLETED, tank(tenantId, 0, 0), tank(tenantId, 0, 0), 100.0);
        when(filtrationRepo.findByIdAndIsDeletedFalse(op.getId())).thenReturn(Optional.of(op));

        assertThrows(IllegalStateException.class, () -> service.deleteFiltration(op.getId()));
        verify(filtrationRepo, never()).save(any());
    }

    @Test
    void operationOfAnotherTenantIsInvisible() {
        FiltrationOperation op = operation(FiltrationStatus.CREATED, tank(tenantId, 0, 0), tank(tenantId, 0, 0), 100.0);
        op.setTenantId(UUID.randomUUID());
        when(filtrationRepo.findByIdAndIsDeletedFalse(op.getId())).thenReturn(Optional.of(op));

        assertThrows(IllegalArgumentException.class, () -> service.deleteFiltration(op.getId()));
    }

    @Test
    void completionKeepsCostOfLostOilInFilteredTank() {
        StorageUnit source = tank(tenantId, 1000.0, 5.0);
        StorageUnit target = tank(tenantId, 0.0, 0.0);
        FiltrationOperation op = operation(FiltrationStatus.IN_PROGRESS, source, target, 100.0);
        when(filtrationRepo.findByIdAndIsDeletedFalse(op.getId())).thenReturn(Optional.of(op));
        when(storageUnitRepo.findByIdAndIsDeletedFalse(source.getId())).thenReturn(Optional.of(source));
        when(storageUnitRepo.findByIdAndIsDeletedFalse(target.getId())).thenReturn(Optional.of(target));
        when(businessCodeGenerator.generate(eq(FiltrationOperation.class), anyString(), anyString())).thenReturn("FI0001");
        when(oilTransactionService.findByStorageUnitId(source.getId())).thenReturn(List.of());

        FiltrationCompletionDto completion = new FiltrationCompletionDto();
        completion.setVolumeAfter(90.0);
        service.completeFiltration(op.getId(), completion);

        assertEquals(900.0, source.getCurrentVolume());
        assertEquals(4500.0, source.getTotalCost());
        assertEquals(90.0, target.getCurrentVolume());
        assertEquals(500.0, target.getTotalCost(), 0.01);
        assertEquals(10.0, op.getLossVolume());
        assertEquals(FiltrationStatus.COMPLETED, op.getStatus());
    }

    @Test
    void completionRefusesVolumeAboveFilteredVolume() {
        FiltrationOperation op = operation(FiltrationStatus.IN_PROGRESS, tank(tenantId, 1000, 5), tank(tenantId, 0, 0), 100.0);
        when(filtrationRepo.findByIdAndIsDeletedFalse(op.getId())).thenReturn(Optional.of(op));
        FiltrationCompletionDto completion = new FiltrationCompletionDto();
        completion.setVolumeAfter(120.0);

        assertThrows(IllegalArgumentException.class, () -> service.completeFiltration(op.getId(), completion));
    }

    private FiltrationRequestDto request(UUID source, UUID target, double volume) {
        FiltrationRequestDto req = new FiltrationRequestDto();
        req.setSource(source);
        req.setTarget(target);
        req.setVolumeToFilter(volume);
        return req;
    }

    private FiltrationOperation operation(FiltrationStatus status, StorageUnit source, StorageUnit target, double volume) {
        FiltrationOperation op = new FiltrationOperation();
        op.setId(UUID.randomUUID());
        op.setTenantId(tenantId);
        op.setStatus(status);
        op.setSourceStorageUnit(source);
        op.setTargetStorageUnit(target);
        op.setVolumeToFilter(volume);
        return op;
    }

    private StorageUnit tank(UUID owner, double volume, double averageCost) {
        StorageUnit tank = new StorageUnit();
        tank.setId(UUID.randomUUID());
        tank.setTenantId(owner);
        tank.setName("Cuve");
        tank.setMaxCapacity(2000.0);
        tank.setCurrentVolume(volume);
        tank.setAvgCost(averageCost);
        tank.setTotalCost(volume * averageCost);
        return tank;
    }
}
