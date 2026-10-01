package com.xdev.ooms.production.maintenance.service;

import com.xdev.ooms.production.maintenance.enums.MaintenanceAssetType;
import com.xdev.ooms.production.maintenance.enums.MaintenanceWorkOrderStatus;
import com.xdev.ooms.production.maintenance.repository.MaintenanceWorkOrderRepository;
import com.xdev.ooms.production.millmachine.entity.MillMachine;
import com.xdev.ooms.production.millmachine.repository.MillMachineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MillMachineAvailabilityServiceTest {

    @Mock
    private MillMachineRepository machineRepository;
    @Mock
    private MaintenanceWorkOrderRepository workOrderRepository;

    private MillMachineAvailabilityService service;

    @BeforeEach
    void setUp() {
        service = new MillMachineAvailabilityService(machineRepository, workOrderRepository);
    }

    @Test
    void refreshReopensBlockedMillWhenNoWorkOrderRemains() {
        assertReopened("INACTIVE");
        assertReopened("OUT_OF_SERVICE");
    }

    @Test
    void refreshClearsMaintenanceWhenNoWorkOrderRemains() {
        UUID id = UUID.randomUUID();
        MillMachine machine = machine(id, "MAINTENANCE");
        when(machineRepository.findById(id)).thenReturn(Optional.of(machine));
        when(workOrderRepository.existsByAssetTypeAndAssetIdAndStatusIn(
                eq(MaintenanceAssetType.MILL_MACHINE), eq(id), eq(activeStatuses())))
                .thenReturn(false);

        service.refreshMillOperatingStatus(id);

        assertEquals("OPERATIONAL", machine.getOperatingStatus());
        verify(machineRepository).save(machine);
    }

    private void assertReopened(String status) {
        UUID id = UUID.randomUUID();
        MillMachine machine = machine(id, status);
        when(machineRepository.findById(id)).thenReturn(Optional.of(machine));
        when(workOrderRepository.existsByAssetTypeAndAssetIdAndStatusIn(
                eq(MaintenanceAssetType.MILL_MACHINE), eq(id), eq(activeStatuses())))
                .thenReturn(false);

        service.refreshMillOperatingStatus(id);

        assertEquals("OPERATIONAL", machine.getOperatingStatus());
        verify(machineRepository).save(machine);
    }

    private MillMachine machine(UUID id, String status) {
        MillMachine machine = new MillMachine();
        machine.setId(id);
        machine.setOperatingStatus(status);
        return machine;
    }

    private List<MaintenanceWorkOrderStatus> activeStatuses() {
        return List.of(MaintenanceWorkOrderStatus.PLANNED, MaintenanceWorkOrderStatus.IN_PROGRESS);
    }
}
