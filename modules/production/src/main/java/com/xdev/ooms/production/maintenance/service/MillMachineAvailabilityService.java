package com.xdev.ooms.production.maintenance.service;

import com.xdev.ooms.production.maintenance.enums.MaintenanceAssetType;
import com.xdev.ooms.production.maintenance.enums.MaintenanceWorkOrderStatus;
import com.xdev.ooms.production.maintenance.repository.MaintenanceWorkOrderRepository;
import com.xdev.ooms.production.millmachine.entity.MillMachine;
import com.xdev.ooms.production.millmachine.repository.MillMachineRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class MillMachineAvailabilityService {

    private static final Set<String> BLOCKED_OPERATING_STATUSES = Set.of(
            "MAINTENANCE",
            "OUT_OF_SERVICE",
            "INACTIVE"
    );

    private static final List<MaintenanceWorkOrderStatus> ACTIVE_MAINTENANCE_STATUSES = List.of(
            MaintenanceWorkOrderStatus.PLANNED,
            MaintenanceWorkOrderStatus.IN_PROGRESS
    );

    private final MillMachineRepository millMachineRepository;
    private final MaintenanceWorkOrderRepository maintenanceWorkOrderRepository;

    public MillMachineAvailabilityService(
            MillMachineRepository millMachineRepository,
            MaintenanceWorkOrderRepository maintenanceWorkOrderRepository) {
        this.millMachineRepository = millMachineRepository;
        this.maintenanceWorkOrderRepository = maintenanceWorkOrderRepository;
    }

    public boolean isAvailableForPlanning(UUID millId) {
        return getBlockingReason(millId).isEmpty();
    }

    public Optional<String> getBlockingReason(UUID millId) {
        MillMachine machine = millMachineRepository.findById(millId)
                .orElseThrow(() -> new EntityNotFoundException("Mill machine not found"));

        String operatingStatus = normalize(machine.getOperatingStatus());
        if (operatingStatus != null && BLOCKED_OPERATING_STATUSES.contains(operatingStatus)) {
            return Optional.of("Mill is " + machine.getOperatingStatus().toLowerCase(Locale.ROOT).replace('_', ' '));
        }

        if (maintenanceWorkOrderRepository.existsByAssetTypeAndAssetIdAndStatusIn(
                MaintenanceAssetType.MILL_MACHINE,
                millId,
                ACTIVE_MAINTENANCE_STATUSES)) {
            return Optional.of("An active maintenance work order blocks this mill");
        }

        return Optional.empty();
    }

    public void assertAvailableForPlanning(UUID millId, String millName) {
        getBlockingReason(millId).ifPresent(reason -> {
            String label = millName != null && !millName.isBlank() ? millName : millId.toString();
            throw new ValidationException("Mill '" + label + "' is not available for planning: " + reason);
        });
    }

    public void refreshMillOperatingStatus(UUID millId) {
        MillMachine machine = millMachineRepository.findById(millId)
                .orElseThrow(() -> new EntityNotFoundException("Mill machine not found"));

        boolean hasActiveMaintenance = maintenanceWorkOrderRepository.existsByAssetTypeAndAssetIdAndStatusIn(
                MaintenanceAssetType.MILL_MACHINE,
                millId,
                ACTIVE_MAINTENANCE_STATUSES);

        if (hasActiveMaintenance) {
            machine.setOperatingStatus("MAINTENANCE");
        } else if (machine.getOperatingStatus() == null
                || BLOCKED_OPERATING_STATUSES.contains(normalize(machine.getOperatingStatus()))) {
            machine.setOperatingStatus("OPERATIONAL");
        }

        millMachineRepository.save(machine);
    }

    private String normalize(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }
}
