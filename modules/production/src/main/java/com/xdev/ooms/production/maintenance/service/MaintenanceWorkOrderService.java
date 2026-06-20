package com.xdev.ooms.production.maintenance.service;

import com.xdev.ooms.inventory.Enum.Statue;
import com.xdev.ooms.inventory.ligneconditionnement.entity.LigneConditionnement;
import com.xdev.ooms.inventory.ligneconditionnement.repository.LigneConditionnementRepository;
import com.xdev.ooms.production.maintenance.dto.MaintenanceWorkOrderDto;
import com.xdev.ooms.production.maintenance.entity.MaintenanceWorkOrder;
import com.xdev.ooms.production.maintenance.enums.MaintenanceAssetType;
import com.xdev.ooms.production.maintenance.enums.MaintenanceWorkOrderStatus;
import com.xdev.ooms.production.maintenance.repository.MaintenanceWorkOrderRepository;
import com.xdev.ooms.production.millmachine.entity.MillMachine;
import com.xdev.ooms.production.millmachine.repository.MillMachineRepository;
import com.xdev.ooms.production.storageunit.entity.StorageUnit;
import com.xdev.ooms.production.storageunit.repository.StorageUnitRepo;
import com.xdev.ooms.sharedkernel.Enum.ExpenseCategory;
import com.xdev.ooms.sharedkernel.Enum.PaymentMethod;
import com.xdev.ooms.sharedkernel.Enum.StorageStatus;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.ports.ExpensePort;
import com.xdev.ooms.sharedkernel.ports.ExpenseRecordCommand;
import com.xdev.ooms.sharedkernel.ports.NotificationEvent;
import com.xdev.ooms.sharedkernel.ports.NotificationPort;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.utils.AuditHelper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class MaintenanceWorkOrderService extends BaseServiceImpl<MaintenanceWorkOrder, MaintenanceWorkOrderDto, MaintenanceWorkOrderDto> {

    private final MaintenanceWorkOrderRepository maintenanceWorkOrderRepository;
    private final MillMachineRepository millMachineRepository;
    private final StorageUnitRepo storageUnitRepo;
    private final LigneConditionnementRepository ligneConditionnementRepository;
    private final ExpensePort expensePort;
    private final NotificationPort notificationPort;

    public MaintenanceWorkOrderService(
            BaseRepository<MaintenanceWorkOrder> repository,
            ModelMapper modelMapper,
            MaintenanceWorkOrderRepository maintenanceWorkOrderRepository,
            MillMachineRepository millMachineRepository,
            StorageUnitRepo storageUnitRepo,
            LigneConditionnementRepository ligneConditionnementRepository,
            ExpensePort expensePort,
            NotificationPort notificationPort) {
        super(repository, modelMapper);
        this.maintenanceWorkOrderRepository = maintenanceWorkOrderRepository;
        this.millMachineRepository = millMachineRepository;
        this.storageUnitRepo = storageUnitRepo;
        this.ligneConditionnementRepository = ligneConditionnementRepository;
        this.expensePort = expensePort;
        this.notificationPort = notificationPort;
    }

    @Override
    public Set<Action> actionsMapping(MaintenanceWorkOrder workOrder) {
        Set<Action> actions = new HashSet<>();
        actions.add(Action.READ);
        if (workOrder.getStatus() != MaintenanceWorkOrderStatus.COMPLETED
                && workOrder.getStatus() != MaintenanceWorkOrderStatus.CANCELLED) {
            actions.add(Action.UPDATE);
        }
        actions.add(Action.DELETE);
        return actions;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaintenanceWorkOrderDto save(MaintenanceWorkOrderDto request) {
        validateRequest(request);
        normalizeCosts(request);
        promoteStatusWhenBillable(request);
        resolveAssetName(request);

        MaintenanceWorkOrder entity = modelMapper.map(request, MaintenanceWorkOrder.class);
        entity.setTotalCost(request.getTotalCost());
        applyCompletionTimestamp(entity);
        AuditHelper.applyAuditOnCreate(entity);

        MaintenanceWorkOrder saved = maintenanceWorkOrderRepository.save(entity);
        applyAssetUpdates(saved);
        boolean expenseRecorded = maybeRecordExpense(saved);
        publishMaintenanceNotifications(saved, true, expenseRecorded);

        return toResponseDto(saved.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaintenanceWorkOrderDto update(MaintenanceWorkOrderDto request) {
        if (request.getId() == null) {
            throw new ValidationException("Maintenance work order id is required");
        }

        MaintenanceWorkOrder existing = maintenanceWorkOrderRepository.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Maintenance work order not found"));

        validateRequest(request);
        normalizeCosts(request);
        promoteStatusWhenBillable(request);
        resolveAssetName(request);

        existing.setAssetType(request.getAssetType());
        existing.setAssetId(request.getAssetId());
        existing.setAssetName(request.getAssetName());
        existing.setMaintenanceType(request.getMaintenanceType());
        existing.setStatus(request.getStatus());
        existing.setScheduledStart(request.getScheduledStart());
        existing.setScheduledEnd(request.getScheduledEnd());
        existing.setTechnician(request.getTechnician());
        existing.setVendor(request.getVendor());
        existing.setDescription(request.getDescription());
        existing.setPartsReplaced(request.getPartsReplaced());
        existing.setPartsCost(request.getPartsCost());
        existing.setLaborCost(request.getLaborCost());
        existing.setTotalCost(request.getTotalCost());
        existing.setPaymentMethod(request.getPaymentMethod());
        existing.setNotes(request.getNotes());

        applyCompletionTimestamp(existing);
        applyAssetUpdates(existing);

        MaintenanceWorkOrder saved = maintenanceWorkOrderRepository.save(existing);
        boolean expenseRecorded = maybeRecordExpense(saved);
        publishMaintenanceNotifications(saved, false, expenseRecorded);

        return toResponseDto(saved.getId());
    }

    public String buildSuccessMessage(MaintenanceWorkOrderDto dto) {
        if (dto == null) {
            return "Maintenance work order saved successfully";
        }
        if (dto.getInvoiceReference() != null && !dto.getInvoiceReference().isBlank()) {
            return "Maintenance saved. Expense recorded: " + dto.getInvoiceReference();
        }
        if (dto.getTotalCost() != null && dto.getTotalCost() > 0
                && dto.getStatus() == MaintenanceWorkOrderStatus.PLANNED) {
            return "Maintenance saved. Set status to In progress or Completed to record the expense.";
        }
        return "Maintenance work order saved successfully";
    }

    private MaintenanceWorkOrderDto toResponseDto(java.util.UUID id) {
        MaintenanceWorkOrder fresh = maintenanceWorkOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Maintenance work order not found"));
        return modelMapper.map(fresh, MaintenanceWorkOrderDto.class);
    }

    private void promoteStatusWhenBillable(MaintenanceWorkOrderDto request) {
        if (request.getTotalCost() != null
                && request.getTotalCost() > 0
                && request.getStatus() == MaintenanceWorkOrderStatus.PLANNED) {
            request.setStatus(MaintenanceWorkOrderStatus.IN_PROGRESS);
        }
    }

    private void validateRequest(MaintenanceWorkOrderDto request) {
        if (request == null) {
            throw new ValidationException("Maintenance request is required");
        }
        if (request.getAssetType() == null) {
            throw new ValidationException("Asset type is required");
        }
        if (request.getAssetId() == null) {
            throw new ValidationException("Asset is required");
        }
        if (request.getMaintenanceType() == null) {
            throw new ValidationException("Maintenance type is required");
        }
        if (request.getStatus() == null) {
            request.setStatus(MaintenanceWorkOrderStatus.PLANNED);
        }
        if (request.getScheduledStart() == null) {
            throw new ValidationException("Scheduled start date is required");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new ValidationException("Description is required");
        }
        if (request.getTechnician() == null || request.getTechnician().isBlank()) {
            throw new ValidationException("Technician is required");
        }
        if (hasParts(request) && (request.getPartsCost() == null || request.getPartsCost() <= 0)) {
            throw new ValidationException("Parts cost is required when parts are listed");
        }
        if (requiresExpense(request) && request.getPaymentMethod() == null) {
            request.setPaymentMethod(PaymentMethod.CASH);
        }
        if (request.getTotalCost() != null
                && request.getTotalCost() > 0
                && request.getStatus() == MaintenanceWorkOrderStatus.CANCELLED) {
            throw new ValidationException("Cannot record costs on a cancelled maintenance work order");
        }
        if (requiresExpense(request)
                && (request.getVendor() == null || request.getVendor().isBlank())
                && (request.getTechnician() == null || request.getTechnician().isBlank())) {
            throw new ValidationException("Vendor or technician is required when recording maintenance costs");
        }
    }

    private void normalizeCosts(MaintenanceWorkOrderDto request) {
        double parts = request.getPartsCost() == null ? 0d : request.getPartsCost();
        double labor = request.getLaborCost() == null ? 0d : request.getLaborCost();
        if (parts < 0 || labor < 0) {
            throw new ValidationException("Costs must be positive");
        }
        request.setPartsCost(parts);
        request.setLaborCost(labor);
        request.setTotalCost(parts + labor);
    }

    private void resolveAssetName(MaintenanceWorkOrderDto request) {
        switch (request.getAssetType()) {
            case MILL_MACHINE -> {
                MillMachine machine = millMachineRepository.findById(request.getAssetId())
                        .orElseThrow(() -> new EntityNotFoundException("Mill machine not found"));
                request.setAssetName(machine.getName());
            }
            case STORAGE_UNIT -> {
                StorageUnit unit = storageUnitRepo.findById(request.getAssetId())
                        .orElseThrow(() -> new EntityNotFoundException("Storage unit not found"));
                request.setAssetName(unit.getName());
            }
            case LIGNE_CONDITIONNEMENT -> {
                LigneConditionnement ligne = ligneConditionnementRepository.findById(request.getAssetId())
                        .orElseThrow(() -> new EntityNotFoundException("Packaging line not found"));
                request.setAssetName(ligne.getNom());
            }
            default -> throw new ValidationException("Unsupported asset type");
        }
    }

    private void applyCompletionTimestamp(MaintenanceWorkOrder entity) {
        if (entity.getStatus() == MaintenanceWorkOrderStatus.COMPLETED && entity.getCompletedAt() == null) {
            entity.setCompletedAt(LocalDateTime.now());
        }
        if (entity.getStatus() != MaintenanceWorkOrderStatus.COMPLETED) {
            entity.setCompletedAt(null);
        }
    }

    private void applyAssetUpdates(MaintenanceWorkOrder entity) {
        LocalDateTime lastMaintenance = entity.getScheduledStart();
        LocalDateTime nextMaintenance = entity.getScheduledEnd() != null
                ? entity.getScheduledEnd()
                : entity.getScheduledStart().plusDays(30);
        boolean underMaintenance = entity.getStatus() == MaintenanceWorkOrderStatus.PLANNED
                || entity.getStatus() == MaintenanceWorkOrderStatus.IN_PROGRESS;

        switch (entity.getAssetType()) {
            case MILL_MACHINE -> updateMillMachine(entity.getAssetId(), lastMaintenance, nextMaintenance, underMaintenance, entity.getStatus());
            case STORAGE_UNIT -> updateStorageUnit(entity.getAssetId(), nextMaintenance, underMaintenance, entity.getStatus());
            case LIGNE_CONDITIONNEMENT -> updateLigneConditionnement(entity.getAssetId(), lastMaintenance, nextMaintenance, underMaintenance, entity.getStatus());
            default -> throw new ValidationException("Unsupported asset type");
        }
    }

    private void updateMillMachine(
            java.util.UUID assetId,
            LocalDateTime lastMaintenance,
            LocalDateTime nextMaintenance,
            boolean underMaintenance,
            MaintenanceWorkOrderStatus status) {
        MillMachine machine = millMachineRepository.findById(assetId)
                .orElseThrow(() -> new EntityNotFoundException("Mill machine not found"));
        machine.setLastMaintenanceDate(lastMaintenance);
        machine.setNextMaintenanceDate(nextMaintenance);
        if (status == MaintenanceWorkOrderStatus.COMPLETED) {
            machine.setOperatingStatus("OPERATIONAL");
        } else if (status == MaintenanceWorkOrderStatus.CANCELLED) {
            if ("MAINTENANCE".equalsIgnoreCase(machine.getOperatingStatus())) {
                machine.setOperatingStatus("OPERATIONAL");
            }
        } else if (underMaintenance) {
            machine.setOperatingStatus("MAINTENANCE");
        }
        millMachineRepository.save(machine);
    }

    private void updateStorageUnit(
            java.util.UUID assetId,
            LocalDateTime nextMaintenance,
            boolean underMaintenance,
            MaintenanceWorkOrderStatus status) {
        StorageUnit unit = storageUnitRepo.findById(assetId)
                .orElseThrow(() -> new EntityNotFoundException("Storage unit not found"));
        unit.setNextMaintenanceDate(nextMaintenance);
        if (status == MaintenanceWorkOrderStatus.COMPLETED || status == MaintenanceWorkOrderStatus.CANCELLED) {
            if (unit.getStatus() == StorageStatus.MAINTENANCE) {
                unit.setStatus(StorageStatus.AVAILABLE);
            }
        } else if (underMaintenance) {
            unit.setStatus(StorageStatus.MAINTENANCE);
        }
        storageUnitRepo.save(unit);
    }

    private void updateLigneConditionnement(
            java.util.UUID assetId,
            LocalDateTime lastMaintenance,
            LocalDateTime nextMaintenance,
            boolean underMaintenance,
            MaintenanceWorkOrderStatus status) {
        LigneConditionnement ligne = ligneConditionnementRepository.findById(assetId)
                .orElseThrow(() -> new EntityNotFoundException("Packaging line not found"));
        ligne.setDateDerniereMaintenance(toDate(lastMaintenance));
        ligne.setDateProchaineMaintenance(toDate(nextMaintenance));
        if (status == MaintenanceWorkOrderStatus.COMPLETED || status == MaintenanceWorkOrderStatus.CANCELLED) {
            if (ligne.getEtat() == Statue.EN_MAINTENANCE) {
                ligne.setEtat(Statue.ACTIF);
            }
        } else if (underMaintenance) {
            ligne.setEtat(Statue.EN_MAINTENANCE);
        }
        ligneConditionnementRepository.save(ligne);
    }

    private boolean maybeRecordExpense(MaintenanceWorkOrder workOrder) {
        if (workOrder.getInvoiceReference() != null && !workOrder.getInvoiceReference().isBlank()) {
            return false;
        }
        if (!requiresExpense(workOrder)) {
            return false;
        }

        String vendor = workOrder.getVendor();
        if (vendor == null || vendor.isBlank()) {
            vendor = workOrder.getTechnician();
        }

        String object = "Maintenance " + workOrder.getMaintenanceType() + " - " + workOrder.getAssetName();
        String purchaseNature = workOrder.getPartsReplaced() != null && !workOrder.getPartsReplaced().isBlank()
                ? workOrder.getPartsReplaced()
                : workOrder.getDescription();
        String notes = buildExpenseNotes(workOrder);

        String invoiceReference = expensePort.record(new ExpenseRecordCommand(
                vendor,
                workOrder.getTotalCost(),
                ExpenseCategory.MACHINE_MAINTENANCE_REPAIR,
                workOrder.getPaymentMethod() != null ? workOrder.getPaymentMethod() : PaymentMethod.CASH,
                object,
                purchaseNature,
                notes,
                workOrder.getId() != null ? workOrder.getId().toString() : null));

        workOrder.setInvoiceReference(invoiceReference);
        maintenanceWorkOrderRepository.save(workOrder);
        return true;
    }

    private void publishMaintenanceNotifications(
            MaintenanceWorkOrder workOrder,
            boolean isCreate,
            boolean expenseRecorded) {
        try {
            Map<String, String> fields = new HashMap<>();
            fields.put("maintenanceType", workOrder.getMaintenanceType() != null ? workOrder.getMaintenanceType().name() : "");
            fields.put("status", workOrder.getStatus() != null ? workOrder.getStatus().name() : "");
            fields.put("totalCost", formatAmount(workOrder.getTotalCost()));

            if (isCreate) {
                notificationPort.publish(new NotificationEvent(
                        "MAINTENANCEWORKORDER_CREATED",
                        workOrder.getId(),
                        workOrder.getAssetName(),
                        fields,
                        null,
                        null));
            }
            if (expenseRecorded) {
                fields.put("invoiceRef", workOrder.getInvoiceReference());
                notificationPort.publish(new NotificationEvent(
                        "MAINTENANCEWORKORDER_EXPENSE_RECORDED",
                        workOrder.getId(),
                        workOrder.getAssetName(),
                        fields,
                        null,
                        null));
            }
        } catch (Exception ignored) {
            // Notification failures must not roll back business operations.
        }
    }

    private boolean requiresExpense(MaintenanceWorkOrder workOrder) {
        return workOrder.getTotalCost() != null
                && workOrder.getTotalCost() > 0
                && (workOrder.getStatus() == MaintenanceWorkOrderStatus.COMPLETED
                || workOrder.getStatus() == MaintenanceWorkOrderStatus.IN_PROGRESS);
    }

    private boolean requiresExpense(MaintenanceWorkOrderDto request) {
        return request.getTotalCost() != null
                && request.getTotalCost() > 0
                && (request.getStatus() == MaintenanceWorkOrderStatus.COMPLETED
                || request.getStatus() == MaintenanceWorkOrderStatus.IN_PROGRESS);
    }

    private boolean hasParts(MaintenanceWorkOrderDto request) {
        return request.getPartsReplaced() != null && !request.getPartsReplaced().isBlank();
    }

    private String buildExpenseNotes(MaintenanceWorkOrder workOrder) {
        StringBuilder builder = new StringBuilder();
        builder.append("Asset: ").append(workOrder.getAssetType()).append(" / ").append(workOrder.getAssetName());
        builder.append(" | Work order: ").append(workOrder.getId());
        builder.append(" | Parts: ").append(formatAmount(workOrder.getPartsCost()));
        builder.append(" | Labor: ").append(formatAmount(workOrder.getLaborCost()));
        if (workOrder.getTechnician() != null && !workOrder.getTechnician().isBlank()) {
            builder.append(" | Technician: ").append(workOrder.getTechnician());
        }
        if (workOrder.getNotes() != null && !workOrder.getNotes().isBlank()) {
            builder.append(" | ").append(workOrder.getNotes().trim());
        }
        return builder.toString();
    }

    private String formatAmount(Double amount) {
        return amount == null ? "0" : String.valueOf(amount);
    }

    private Date toDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
