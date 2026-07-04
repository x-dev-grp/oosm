package com.xdev.ooms.production.equipment.service;

import com.xdev.ooms.production.equipment.dto.EquipmentServiceMissionDto;
import com.xdev.ooms.production.equipment.dto.MillEquipmentDto;
import com.xdev.ooms.production.equipment.entity.EquipmentServiceMission;
import com.xdev.ooms.production.equipment.entity.MillEquipment;
import com.xdev.ooms.production.equipment.enums.EquipmentServiceMissionStatus;
import com.xdev.ooms.production.equipment.enums.MillEquipmentStatus;
import com.xdev.ooms.production.equipment.repository.EquipmentServiceMissionRepository;
import com.xdev.ooms.production.equipment.repository.MillEquipmentRepository;
import com.xdev.ooms.sharedkernel.Enum.Currency;
import com.xdev.ooms.sharedkernel.Enum.PaymentMethod;
import com.xdev.ooms.sharedkernel.Enum.ResourceName;
import com.xdev.ooms.sharedkernel.Enum.TransactionDirection;
import com.xdev.ooms.sharedkernel.Enum.TransactionType;
import com.xdev.ooms.sharedkernel.communicator.models.shared.FinancialTransactionDto;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.ports.FinancialTransactionPort;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.utils.AuditHelper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class EquipmentServiceMissionService
        extends BaseServiceImpl<EquipmentServiceMission, EquipmentServiceMissionDto, EquipmentServiceMissionDto> {

    private static final Set<EquipmentServiceMissionStatus> ACTIVE_MISSION_STATUSES =
            EnumSet.of(EquipmentServiceMissionStatus.PLANNED, EquipmentServiceMissionStatus.IN_PROGRESS);

    private final EquipmentServiceMissionRepository missionRepository;
    private final MillEquipmentRepository millEquipmentRepository;
    private final FinancialTransactionPort financialTransactionPort;

    public EquipmentServiceMissionService(
            BaseRepository<EquipmentServiceMission> repository,
            ModelMapper modelMapper,
            EquipmentServiceMissionRepository missionRepository,
            MillEquipmentRepository millEquipmentRepository,
            FinancialTransactionPort financialTransactionPort) {
        super(repository, modelMapper);
        this.missionRepository = missionRepository;
        this.millEquipmentRepository = millEquipmentRepository;
        this.financialTransactionPort = financialTransactionPort;
    }

    @Override
    public Set<Action> actionsMapping(EquipmentServiceMission mission) {
        Set<Action> actions = new HashSet<>();
        actions.add(Action.READ);
        if (mission.getStatus() != EquipmentServiceMissionStatus.COMPLETED
                && mission.getStatus() != EquipmentServiceMissionStatus.CANCELLED) {
            actions.add(Action.UPDATE);
        }
        actions.add(Action.DELETE);
        return actions;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EquipmentServiceMissionDto save(EquipmentServiceMissionDto request) {
        validateRequest(request);
        MillEquipment equipment = resolveEquipment(request.getEquipment());
        normalizeBilling(request, equipment);

        EquipmentServiceMission entity = modelMapper.map(request, EquipmentServiceMission.class);
        entity.setEquipment(equipment);
        applyCompletionRules(entity, null);
        AuditHelper.applyAuditOnCreate(entity);

        applyEquipmentStatusForMission(entity, null);
        EquipmentServiceMission saved = missionRepository.save(entity);
        maybeRecordFinancialTransaction(saved, null);

        return toResponseDto(saved.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EquipmentServiceMissionDto update(EquipmentServiceMissionDto request) {
        if (request.getId() == null) {
            throw new ValidationException("Mission id is required");
        }

        EquipmentServiceMission existing = missionRepository.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Equipment service mission not found"));

        EquipmentServiceMissionStatus previousStatus = existing.getStatus();
        validateRequest(request);
        MillEquipment equipment = resolveEquipment(request.getEquipment());
        normalizeBilling(request, equipment);

        existing.setEquipment(equipment);
        existing.setClientName(request.getClientName());
        existing.setClientPhone(request.getClientPhone());
        existing.setWorkLocation(request.getWorkLocation());
        existing.setDescription(request.getDescription());
        existing.setOperatorName(request.getOperatorName());
        existing.setStatus(request.getStatus());
        existing.setScheduledStart(request.getScheduledStart());
        existing.setScheduledEnd(request.getScheduledEnd());
        existing.setBillableHours(request.getBillableHours());
        existing.setHourlyRate(request.getHourlyRate());
        existing.setTotalAmount(request.getTotalAmount());
        existing.setPaymentMethod(request.getPaymentMethod());
        existing.setPaidAmount(request.getPaidAmount());
        existing.setUnpaidAmount(request.getUnpaidAmount());
        existing.setNotes(request.getNotes());

        applyCompletionRules(existing, previousStatus);
        applyEquipmentStatusForMission(existing, previousStatus);
        EquipmentServiceMission saved = missionRepository.save(existing);
        maybeRecordFinancialTransaction(saved, previousStatus);

        return toResponseDto(saved.getId());
    }

    public String buildSuccessMessage(EquipmentServiceMissionDto dto) {
        if (dto != null && dto.getInvoiceReference() != null && !dto.getInvoiceReference().isBlank()) {
            return "Mission saved. Finance reference: " + dto.getInvoiceReference();
        }
        return "Equipment service mission saved successfully";
    }

    private MillEquipment resolveEquipment(MillEquipmentDto equipmentDto) {
        if (equipmentDto == null || equipmentDto.getId() == null) {
            throw new ValidationException("Equipment is required");
        }
        return millEquipmentRepository.findById(equipmentDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Equipment not found"));
    }

    private void validateRequest(EquipmentServiceMissionDto request) {
        if (request == null) {
            throw new ValidationException("Mission payload is required");
        }
        if (request.getClientName() == null || request.getClientName().isBlank()) {
            throw new ValidationException("Client name is required");
        }
        if (request.getStatus() == null) {
            request.setStatus(EquipmentServiceMissionStatus.PLANNED);
        }
        if (request.getStatus() == EquipmentServiceMissionStatus.COMPLETED) {
            if (request.getBillableHours() == null || request.getBillableHours() <= 0) {
                throw new ValidationException("Billable hours must be greater than zero for completed missions");
            }
        }
    }

    private void normalizeBilling(EquipmentServiceMissionDto request, MillEquipment equipment) {
        if (request.getHourlyRate() == null || request.getHourlyRate() <= 0) {
            request.setHourlyRate(equipment.getDefaultHourlyRate() != null ? equipment.getDefaultHourlyRate() : 0d);
        }
        double hours = request.getBillableHours() != null ? request.getBillableHours() : 0d;
        double rate = request.getHourlyRate() != null ? request.getHourlyRate() : 0d;
        double total = roundMoney(hours * rate);
        request.setTotalAmount(total);

        if (request.getStatus() == EquipmentServiceMissionStatus.COMPLETED && total > 0) {
            double paid = request.getPaidAmount() != null ? request.getPaidAmount() : 0d;
            if (paid < 0) {
                paid = 0d;
            }
            if (paid > total) {
                paid = total;
            }
            request.setPaidAmount(paid);
            request.setUnpaidAmount(roundMoney(total - paid));
        } else if (request.getPaidAmount() == null) {
            request.setPaidAmount(0d);
            request.setUnpaidAmount(total);
        }
    }

    private void applyCompletionRules(EquipmentServiceMission entity, EquipmentServiceMissionStatus previousStatus) {
        if (entity.getStatus() == EquipmentServiceMissionStatus.COMPLETED) {
            if (entity.getCompletedAt() == null) {
                entity.setCompletedAt(LocalDateTime.now());
            }
            entity.setTotalAmount(roundMoney(entity.getBillableHours() * entity.getHourlyRate()));
            double paid = entity.getPaidAmount() != null ? entity.getPaidAmount() : 0d;
            entity.setUnpaidAmount(roundMoney(Math.max(0d, entity.getTotalAmount() - paid)));
        } else if (entity.getStatus() != EquipmentServiceMissionStatus.COMPLETED) {
            entity.setCompletedAt(null);
        }
    }

    private void applyEquipmentStatusForMission(
            EquipmentServiceMission mission,
            EquipmentServiceMissionStatus previousStatus) {
        MillEquipment equipment = mission.getEquipment();
        if (equipment == null) {
            return;
        }

        UUID excludeId = mission.getId();
        if (mission.getStatus() == EquipmentServiceMissionStatus.IN_PROGRESS) {
            if (equipment.getStatus() == MillEquipmentStatus.OUT_OF_SERVICE) {
                throw new ValidationException("Equipment is out of service");
            }
            if (equipment.getStatus() == MillEquipmentStatus.IN_USE
                    && !EquipmentServiceMissionStatus.IN_PROGRESS.equals(previousStatus)) {
                throw new ValidationException("Equipment is already in use");
            }
            if (excludeId == null) {
                if (missionRepository.existsByEquipment_IdAndStatusIn(equipment.getId(), ACTIVE_MISSION_STATUSES)) {
                    throw new ValidationException("Equipment already has an active mission");
                }
            } else if (missionRepository.existsByEquipment_IdAndStatusInAndIdNot(
                    equipment.getId(), ACTIVE_MISSION_STATUSES, excludeId)) {
                throw new ValidationException("Equipment already has an active mission");
            }
            equipment.setStatus(MillEquipmentStatus.IN_USE);
            millEquipmentRepository.save(equipment);
            return;
        }

        if (mission.getStatus() == EquipmentServiceMissionStatus.COMPLETED) {
            double hours = mission.getBillableHours() != null ? mission.getBillableHours() : 0d;
            equipment.setHoursOperated(roundMoney((equipment.getHoursOperated() != null ? equipment.getHoursOperated() : 0d) + hours));
            equipment.setStatus(MillEquipmentStatus.AVAILABLE);
            millEquipmentRepository.save(equipment);
            return;
        }

        if (mission.getStatus() == EquipmentServiceMissionStatus.CANCELLED
                && previousStatus == EquipmentServiceMissionStatus.IN_PROGRESS) {
            equipment.setStatus(MillEquipmentStatus.AVAILABLE);
            millEquipmentRepository.save(equipment);
        }
    }

    private boolean maybeRecordFinancialTransaction(
            EquipmentServiceMission mission,
            EquipmentServiceMissionStatus previousStatus) {
        if (mission.getStatus() != EquipmentServiceMissionStatus.COMPLETED) {
            return false;
        }
        if (previousStatus == EquipmentServiceMissionStatus.COMPLETED) {
            return false;
        }
        if (mission.getInvoiceReference() != null && !mission.getInvoiceReference().isBlank()) {
            return false;
        }
        if (mission.getTotalAmount() == null || mission.getTotalAmount() <= 0) {
            return false;
        }

        String reference = "EQM-" + mission.getId().toString().substring(0, 8).toUpperCase();
        FinancialTransactionDto tx = new FinancialTransactionDto();
        tx.setTransactionType(TransactionType.EQUIPMENT_SERVICE);
        tx.setDirection(TransactionDirection.INBOUND);
        tx.setAmount(BigDecimal.valueOf(mission.getTotalAmount()).setScale(2, RoundingMode.HALF_UP));
        tx.setCurrency(Currency.TND);
        tx.setPaymentMethod(mission.getPaymentMethod() != null ? mission.getPaymentMethod() : PaymentMethod.CASH);
        tx.setTransactionDate(LocalDateTime.now());
        tx.setApproved(true);
        tx.setApprovalDate(LocalDateTime.now());
        tx.setExternalTransactionId(mission.getId().toString());
        tx.setResourceName(ResourceName.EquipmentServiceMission);
        tx.setInvoiceReference(reference);
        tx.setDescription(buildTransactionDescription(mission));
        tx.setPaidAmount(mission.getPaidAmount());
        tx.setUnpaidAmount(mission.getUnpaidAmount());
        tx.setSyncProductionState(false);

        financialTransactionPort.record(tx);

        mission.setInvoiceReference(reference);
        missionRepository.save(mission);
        return true;
    }

    private String buildTransactionDescription(EquipmentServiceMission mission) {
        String equipmentName = mission.getEquipment() != null ? mission.getEquipment().getName() : "Equipment";
        return "Equipment service - " + equipmentName + " - " + mission.getClientName();
    }

    private double roundMoney(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private EquipmentServiceMissionDto toResponseDto(UUID id) {
        EquipmentServiceMission entity = missionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Equipment service mission not found"));
        return modelMapper.map(entity, EquipmentServiceMissionDto.class);
    }
}
