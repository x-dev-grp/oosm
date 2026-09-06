package com.xdev.ooms.production.unifieddelivery.service;

import com.xdev.ooms.production.oiltransaction.entity.OilTransaction;

import com.xdev.ooms.sharedkernel.basetype.entity.BaseType;
import com.xdev.ooms.sharedkernel.basetype.repository.GenericRepository;
import com.xdev.ooms.production.oiltransaction.service.OilTransactionService;
import com.xdev.ooms.production.parameter.service.ReceptionLimitsParameterReader;
import com.xdev.ooms.production.qualitycontrol.entity.QualityControlResult;
import com.xdev.ooms.production.qualitycontrol.repository.QualityControlResultRepository;
import com.xdev.ooms.production.storageunit.dto.StorageUnitDto;
import com.xdev.ooms.production.storageunit.entity.StorageUnit;
import com.xdev.ooms.production.storageunit.repository.StorageUnitRepo;
import com.xdev.ooms.production.supplier.dto.SupplierDto;
import com.xdev.ooms.production.supplier.entity.Supplier;
import com.xdev.ooms.production.supplier.repository.SupplierRepository;
import com.xdev.ooms.production.unifieddelivery.dto.PaymentDTO;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;
import com.xdev.ooms.production.unifieddelivery.repository.DeliveryRepository;



import com.xdev.ooms.production.unifieddelivery.dto.ExchangePricingDto;
import com.xdev.ooms.production.unifieddelivery.dto.NextDeliveryNumbersDto;
import com.xdev.ooms.production.unifieddelivery.dto.UnifiedDeliveryDTO;


import  com.xdev.ooms.sharedkernel.Enum.*;
import  com.xdev.ooms.sharedkernel.Enum.Currency;
import com.xdev.ooms.sharedkernel.communicator.models.shared.FinancialTransactionDto;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.ports.FinancialTransactionPort;
import com.xdev.ooms.sharedkernel.ports.NotificationEvent;
import com.xdev.ooms.sharedkernel.ports.NotificationPort;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import jakarta.persistence.EntityNotFoundException;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UnifiedDeliveryService extends BaseServiceImpl<UnifiedDelivery, UnifiedDeliveryDTO, UnifiedDeliveryDTO> {

    private static final Logger log = LoggerFactory.getLogger(UnifiedDeliveryService.class);

    public static final String DELIVERY_NUMBER = "deliveryNumber";
    public static final String D = "%03d";
    public static final String D1 = "%02d";
    public static final String ID = "id";
    public static final String SUPPLIER = "supplier";
    public static final String STORAGE_UNIT = "storageUnit";
    public static final String PAID = "paid";
    private final DeliveryRepository deliveryRepository;
    private final SupplierRepository supplierRepository;
    private final StorageUnitRepo storageUnitRepo;
    private final GenericRepository genericRepository;
    private final OilTransactionService oilTransactionService;
    private final FinancialTransactionPort financialTransactionPort;
    private final QualityControlResultRepository qualityControlResultRepository;
    private final NotificationPort notificationPort;
    private final ReceptionLimitsParameterReader receptionLimitsParameterReader;

    public UnifiedDeliveryService(BaseRepository<UnifiedDelivery> repository, ModelMapper modelMapper, DeliveryRepository deliveryRepository, SupplierRepository supplierRepository, StorageUnitRepo storageUnitRepo, GenericRepository genericRepository, OilTransactionService oilTransactionService, FinancialTransactionPort financialTransactionPort, QualityControlResultRepository qualityControlResultRepository, NotificationPort notificationPort, ReceptionLimitsParameterReader receptionLimitsParameterReader) {
        super(repository, modelMapper);
        this.deliveryRepository = deliveryRepository;
        this.supplierRepository = supplierRepository;
        this.storageUnitRepo = storageUnitRepo;
        this.genericRepository = genericRepository;
        this.oilTransactionService = oilTransactionService;
        this.financialTransactionPort = financialTransactionPort;
        this.qualityControlResultRepository = qualityControlResultRepository;
        this.notificationPort = notificationPort;
        this.receptionLimitsParameterReader = receptionLimitsParameterReader;
    }

    private static double r3(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }

    private static double safe(Double v) {
        return (v == null || v.isNaN() || v.isInfinite()) ? 0.0 : v;
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    // Helper: treat null/0/<=0 as invalid
    private static boolean isValidPoids(java.math.BigDecimal w) {
        return w != null && w.compareTo(java.math.BigDecimal.ZERO) > 0;
    }

    /**
     * Determines if a delivery is fully paid by comparing unpaid amount with a tolerance threshold.
     * This method is critical for payment workflow decisions.
     *
     * @param delivery The delivery to check for payment status
     * @return true if the delivery is considered fully paid, false otherwise
     */
    private boolean isFullyPaid(UnifiedDelivery delivery) {
        if (delivery == null) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.WARN, "[isFullyPaid] Delivery is null, returning false");
            return false;
        }

        // Extract payment amounts with null safety
        double price = Optional.ofNullable(delivery.getPrice()).orElse(0d);
        double paid = Optional.ofNullable(delivery.getPaidAmount()).orElse(0d);
        double unpaid = Optional.ofNullable(delivery.getUnpaidAmount()).orElse(price - paid); // fallback calculation

        // Log payment details for debugging
        OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[isFullyPaid] Payment check for delivery %s: price=%.2f, paid=%.2f, unpaid=%.2f", delivery.getLotNumber(), price, paid, unpaid);

        // Use tolerance for floating point comparison (0.0001 TND = 0.01 centimes)
        boolean fullyPaid = unpaid <= 0.0001;

        OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[isFullyPaid] Delivery %s is %s", delivery.getLotNumber(), fullyPaid ? "FULLY PAID" : "NOT FULLY PAID");

        return fullyPaid;
    }

    @Override
    @Transactional
    public UnifiedDeliveryDTO save(UnifiedDeliveryDTO dto) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "save", dto);
        // Map DTO to entity

        UnifiedDelivery delivery = modelMapper.map(dto, UnifiedDelivery.class);
        if (dto.getDeliveryType() == DeliveryType.OIL) {
            delivery.setStatus(OliveLotStatus.NEW);
        } else if (delivery.getPoidsCamionVide() != null &&delivery.getPoidsCamionVide() != 0 && dto.getDeliveryType() == DeliveryType.OLIVE) {
            delivery.setStatus(OliveLotStatus.NEW);
        } else {
            delivery.setStatus(OliveLotStatus.WAITING);
        }

        if (dto.getSupplier() != null) {
            Supplier supplier = supplierRepository.findById(dto.getSupplier().getId()).orElseThrow(() -> new RuntimeException("Supplier not found with id: " + dto.getSupplier().getId()));
            delivery.setSupplierType(supplier);
        }

        if (dto.getStorageUnit() != null && dto.getStorageUnit().getId() != null) {
            StorageUnit stu = storageUnitRepo.findByIdAndIsDeletedFalse(dto.getStorageUnit().getId())
                    .orElseThrow(() -> new RuntimeException("StorageUnit not found with id: " + dto.getStorageUnit().getId()));
            delivery.setStorageUnit(stu);
        } else {
            delivery.setStorageUnit(null);
        }

        if (dto.getOliveVariety() != null && dto.getOliveVariety().getId() != null) {
            BaseType oliveVariety = genericRepository.findById(dto.getOliveVariety().getId())
                    .orElseThrow(() -> new RuntimeException("OliveVariety not found with id: " + dto.getOliveVariety().getId()));
            delivery.setOliveVariety(oliveVariety);
        } else {
            delivery.setOliveVariety(null);
        }
        if (dto.getOilVariety() != null && dto.getOilVariety().getId() != null) {
            BaseType oilVariety = genericRepository.findById(dto.getOilVariety().getId())
                    .orElseThrow(() -> new RuntimeException("OilVariety not found with id: " + dto.getOilVariety().getId()));
            delivery.setOilVariety(oilVariety);
        } else {
            delivery.setOilVariety(null);
        }

        assignNumbersOnCreate(delivery);

        // Save entity
        UnifiedDelivery savedDelivery = deliveryRepository.saveAndFlush(delivery);
        savedDelivery = ensureQrCodeIfSupported(savedDelivery);
        publishReceptionCreatedNotification(savedDelivery);

        // Map back to DTO and return
        OOSMLogger.logMethodExit(this.getClass(), "save", savedDelivery);
        OOSMLogger.logPerformance(this.getClass(), "save", startTime, System.currentTimeMillis());
        return mapToDto(savedDelivery);
    }

    @Override
    @Transactional
    public UnifiedDeliveryDTO update(UnifiedDeliveryDTO dto) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "update", dto);
        // 1. Load existing or fail
        UnifiedDelivery existing = deliveryRepository.findByIdAndIsDeletedFalse(dto.getId()).orElseThrow(() -> new RuntimeException("UnifiedDelivery not found with id: " + dto.getId()));

        // 2. Copy simple fields (exclude those we manage manually, including status)
        BeanUtils.copyProperties(dto, existing, "id", "supplier", "storageUnit", "paid", "oliveVariety", "oilVariety", "parcel", "status");

        // 3. Resolve Supplier
        if (dto.getSupplier() != null && dto.getSupplier().getId() != null) {
            Supplier supplier = supplierRepository.findByIdAndIsDeletedFalse(dto.getSupplier().getId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + dto.getSupplier().getId()));
            existing.setSupplierType(supplier);
        } else {
            existing.setSupplierType(null);
        }

        // 4. Resolve StorageUnit
        if (dto.getStorageUnit() != null && dto.getStorageUnit().getId() != null) {
            StorageUnit stu = storageUnitRepo.findByIdAndIsDeletedFalse(dto.getStorageUnit().getId())
                    .orElseThrow(() -> new RuntimeException("StorageUnit not found with id: " + dto.getStorageUnit().getId()));
            existing.setStorageUnit(stu);
        } else {
            existing.setStorageUnit(null);
        }

        // 5. Resolve OliveVariety / OilVariety (generic types)
        if (dto.getOliveVariety() != null && dto.getOliveVariety().getId() != null) {
            BaseType oliveVariety = genericRepository.findById(dto.getOliveVariety().getId())
                    .orElseThrow(() -> new RuntimeException("OliveVariety not found with id: " + dto.getOliveVariety().getId()));
            existing.setOliveVariety(oliveVariety);
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[update] Set oliveVariety to: %s (ID: %s)", oliveVariety.getName(), oliveVariety.getId());
        } else {
            existing.setOliveVariety(null);
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[update] Set oliveVariety to null");
        }
        if (dto.getOilVariety() != null && dto.getOilVariety().getId() != null) {
            BaseType oilVariety = genericRepository.findById(dto.getOilVariety().getId())
                    .orElseThrow(() -> new RuntimeException("OilVariety not found with id: " + dto.getOilVariety().getId()));
            existing.setOilVariety(oilVariety);
        } else {
            existing.setOilVariety(null);
        }

        // 6. Merge poidsCamionVide explicitly (do NOT clear it when DTO omits it)
        if (dto.getPoidsCamionVide() != null) {
            if (dto.getPoidsCamionVide() < 0) {
                throw new IllegalArgumentException("poidsCamionVide cannot be negative: " + dto.getPoidsCamionVide());
            }
            existing.setPoidsCamionVide(dto.getPoidsCamionVide());
        }



        // 8. Handle status for OLIVE deliveries
        if (existing.getDeliveryType() == DeliveryType.OLIVE) {
            OliveLotStatus prev = existing.getStatus();

            // Check if poidsCamionVide or status is being updated
            boolean isPoidsUpdated = dto.getPoidsCamionVide() != null;
            boolean isStatusUpdated = dto.getStatus() != null;

            if (isPoidsUpdated || isStatusUpdated) {
                // Apply status logic only if poidsCamionVide or status is explicitly updated
                boolean poidsValid = isValidPoids(existing.getPoidsCamionVide() != null ? BigDecimal.valueOf(existing.getPoidsCamionVide()) : null);
                if (poidsValid && prev == OliveLotStatus.WAITING) {
                    existing.setStatus(OliveLotStatus.NEW);
                    OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO,
                            "[update] poidsCamionVide valid (>0) and previous status WAITING: status -> NEW (prev=%s, poids=%s)",
                            prev, existing.getPoidsCamionVide());
                } else if (!poidsValid && prev == OliveLotStatus.NEW) {
                    existing.setStatus(OliveLotStatus.WAITING);
                    OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO,
                            "[update] poidsCamionVide invalid (null/0/<=0) and previous status NEW: status -> WAITING (prev=%s, poids=%s)",
                            prev, existing.getPoidsCamionVide());
                } else {
                    OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO,
                            "[update] Status unchanged: poidsValid=%s, prev=%s", poidsValid, prev);
                }
            } else {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO,
                        "[update] No update to poidsCamionVide or status: retaining status %s", prev);
            }
        } else if (dto.getStatus() != null) {
            // For non-OLIVE deliveries, allow direct status update
            existing.setStatus(dto.getStatus());
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO,
                    "[update] Non-OLIVE delivery: status updated to %s", dto.getStatus());
        }

        // 9. Persist
        UnifiedDelivery updated = deliveryRepository.save(existing);

        OOSMLogger.logMethodExit(this.getClass(), "update", updated);
        OOSMLogger.logPerformance(this.getClass(), "update", startTime, System.currentTimeMillis());
        return mapToDto(updated);
    }

    @Transactional(readOnly = true)
    public List<UnifiedDeliveryDTO> getForPlanning() {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "getForPlanning", null);
        List<UnifiedDeliveryDTO> result = deliveryRepository.findOliveDeliveriesControlled().stream().map(this::mapToDto).collect(Collectors.toList());
        OOSMLogger.logMethodExit(this.getClass(), "getForPlanning", result);
        OOSMLogger.logPerformance(this.getClass(), "getForPlanning", startTime, System.currentTimeMillis());
        return result;
    }

    @Transactional(readOnly = true)
    public List<UnifiedDeliveryDTO> findByDeliveryTypeInAndQualityControlResultsIsNull(List<String> types) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "findByDeliveryTypeInAndQualityControlResultsIsNull", types);
        List<UnifiedDeliveryDTO> result = deliveryRepository.findByDeliveryTypeInAndQualityControlResultsIsNull(types).stream().map(this::mapToDto).collect(Collectors.toList());
        OOSMLogger.logMethodExit(this.getClass(), "findByDeliveryTypeInAndQualityControlResultsIsNull", result);
        OOSMLogger.logPerformance(this.getClass(), "findByDeliveryTypeInAndQualityControlResultsIsNull", startTime, System.currentTimeMillis());
        return result;
    }

    // Get deliveries by supplier ID
    @Transactional(readOnly = true)
    public List<UnifiedDeliveryDTO> getDeliveriesBySupplier(UUID supplierId) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "getDeliveriesBySupplier", supplierId);
        List<UnifiedDeliveryDTO> result = deliveryRepository.findBySupplierId(supplierId).stream().map(this::mapToDto).collect(Collectors.toList());
        OOSMLogger.logMethodExit(this.getClass(), "getDeliveriesBySupplier", result);
        OOSMLogger.logPerformance(this.getClass(), "getDeliveriesBySupplier", startTime, System.currentTimeMillis());
        return result;
    }

    // Get paid deliveries by supplier ID
    @Transactional(readOnly = true)
    public List<UnifiedDeliveryDTO> getPaidDeliveriesBySupplier(UUID supplierId) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "getPaidDeliveriesBySupplier", supplierId);
        List<UnifiedDeliveryDTO> result = deliveryRepository.findFullyPaidDeliveriesBySupplierId(supplierId).stream().map(this::mapToDto).collect(Collectors.toList());
        OOSMLogger.logMethodExit(this.getClass(), "getPaidDeliveriesBySupplier", result);
        OOSMLogger.logPerformance(this.getClass(), "getPaidDeliveriesBySupplier", startTime, System.currentTimeMillis());
        return result;
    }

    // Get unpaid deliveries by supplier ID
    @Transactional(readOnly = true)
    public List<UnifiedDeliveryDTO> getUnpaidDeliveriesBySupplier(UUID supplierId) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "getUnpaidDeliveriesBySupplier", supplierId);
        List<UnifiedDeliveryDTO> result = deliveryRepository.findUnpaidDeliveriesBySupplierId(supplierId).stream().map(this::mapToDto).collect(Collectors.toList());
        OOSMLogger.logMethodExit(this.getClass(), "getUnpaidDeliveriesBySupplier", result);
        OOSMLogger.logPerformance(this.getClass(), "getUnpaidDeliveriesBySupplier", startTime, System.currentTimeMillis());
        return result;
    }

    @Override
    public Set<Action> actionsMapping(UnifiedDelivery delivery) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "actionsMapping", delivery);
        if (delivery.getDeliveryType() == DeliveryType.OIL) {
            Set<Action> actions = mapOilDeliveryActions(delivery);
            OOSMLogger.logMethodExit(this.getClass(), "actionsMapping", actions);
            OOSMLogger.logPerformance(this.getClass(), "actionsMapping", startTime, System.currentTimeMillis());
            return actions;
        } else {
            Set<Action> actions = mapOliveDeliveryActions(delivery);
            OOSMLogger.logMethodExit(this.getClass(), "actionsMapping", actions);
            OOSMLogger.logPerformance(this.getClass(), "actionsMapping", startTime, System.currentTimeMillis());
            return actions;
        }
    }

    /**
     * Maps available actions for olive deliveries based on their status and operation type.
     * This method is crucial for determining the payment workflow steps.
     *
     * @param delivery The olive delivery to map actions for
     * @return Set of available actions for the delivery
     */
    private Set<Action> mapOliveDeliveryActions(UnifiedDelivery delivery) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "mapOliveDeliveryActions", delivery);

        if (delivery == null) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.WARN, "[mapOliveDeliveryActions] Delivery is null, returning empty action set");
            return new HashSet<>();
        }

        Set<Action> actions = new HashSet<>();
        actions.add(Action.READ);
        actions.add(Action.REGENERATE_QR);

        switch (delivery.getStatus()) {
            case WAITING -> {
                actions.addAll(Set.of(Action.DELETE, Action.UPDATE));
            }
            case NEW -> {
                actions.addAll(Set.of(Action.DELETE, Action.UPDATE, Action.OLIVE_QUALITY));
                actions.add(Action.GEN_PDF);
            }
            case PROD_READY -> {
                actions.add(Action.DELETE);
                actions.add(Action.GEN_PDF);
                actions.add(Action.GEN_PDF_QC_OLIVE);
                if (!receptionLimitsParameterReader.isMillPlanningEnabled()) {
                    actions.add(Action.COMPLETE);
                }
            }
            case OLIVE_CONTROLLED -> {
                actions.addAll(Set.of(Action.DELETE, Action.UPDATE, Action.GEN_PDF_QC_OLIVE));
                actions.add(Action.GEN_PDF);

                switch (delivery.getOperationType()) {
                    case EXCHANGE, OLIVE_PURCHASE -> {
                        actions.add(Action.SET_PRICE);
                    }
                    default -> {
                    }
                }
                if (!receptionLimitsParameterReader.isMillPlanningEnabled()) {
                    actions.add(Action.COMPLETE);
                }
            }
            case IN_PROGRESS -> {
                actions.add(Action.GEN_PDF);
                actions.add(Action.GEN_PDF_QC_OLIVE);
                if (!receptionLimitsParameterReader.isMillPlanningEnabled()) {
                    actions.add(Action.COMPLETE);
                }
            }
            case COMPLETED -> {
                actions.add(Action.GEN_PDF_QC_OLIVE);
                actions.add(Action.GEN_PDF_PRODUCTION); // ✅ Bon de production quand réception olive terminée
//                actions.add(Action.GEN_INVOICE);
                actions.add(Action.GEN_PDF);

                switch (delivery.getOperationType()) {
                    case SIMPLE_RECEPTION -> {
                        // CRITICAL: Check payment status for simple reception
                        OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[mapOliveDeliveryActions] SIMPLE_RECEPTION payment check for delivery %s: fullyPaid=%s", delivery.getLotNumber(), delivery.getPaid());
                        actions.add(Action.GEN_PDF);

                        if (!delivery.getPaid()) {
                            actions.add(Action.PAY);
                            if (!hasActivePaymentOilLeg(delivery)) {
                                actions.add(Action.OIL_QUALITY);
                                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[mapOliveDeliveryActions] Added OIL_QUALITY action for unpaid delivery " + delivery.getLotNumber());
                            } else {
                                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[mapOliveDeliveryActions] Skipped OIL_QUALITY — payment oil leg already exists for " + delivery.getLotNumber());
                            }
                        }
                    }
                    case BASE, OLIVE_PURCHASE -> {
                        OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[mapOliveDeliveryActions] Adding OIL_RECEPTION for %s operation delivery %s", delivery.getOperationType(), delivery.getLotNumber());

                        actions.add(Action.OIL_RECEPTION);
                        actions.add(Action.GEN_PDF_QC_OIL);
                        actions.add(Action.GEN_PDF);

                        if (!delivery.getPaid()) {
                            actions.add(Action.PAY);
                            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[mapOliveDeliveryActions] Added OIL_QUALITY action for unpaid delivery " + delivery.getLotNumber());
                        }
                    }
                }
            }
            case IN_STOCK -> {
                // ✅ Même logique que COMPLETED → possibilité de générer bon de production
                actions.add(Action.GEN_PDF_PRODUCTION);
//                actions.add(Action.GEN_INVOICE);
                actions.add(Action.GEN_PDF);

            }
        }

        OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[mapOliveDeliveryActions] Final actions for delivery %s: %s", delivery.getLotNumber(), actions);
        OOSMLogger.logMethodExit(this.getClass(), "mapOliveDeliveryActions", actions);
        OOSMLogger.logPerformance(this.getClass(), "mapOliveDeliveryActions", startTime, System.currentTimeMillis());
        return actions;
    }


    /**
     * Maps available actions for oil deliveries based on their status.
     * This method handles the oil delivery workflow including quality control and payment processing.
     *
     * @param delivery The oil delivery to map actions for
     * @return Set of available actions for the delivery
     */
    private Set<Action> mapOilDeliveryActions(UnifiedDelivery delivery) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "mapOilDeliveryActions", delivery);

        if (delivery == null) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.WARN, "[mapOilDeliveryActions] Delivery is null, returning empty action set");
            return new HashSet<>();
        }

        Set<Action> actions = new HashSet<>();
        actions.add(Action.READ);
        actions.add(Action.REGENERATE_QR);


        OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[mapOilDeliveryActions] Mapping actions for oil delivery %s (Status: %s, Operation: %s)", delivery.getLotNumber(), delivery.getStatus(), delivery.getOperationType());

        switch (delivery.getStatus()) {
            case NEW -> {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[mapOilDeliveryActions] Adding NEW status actions for oil delivery " + delivery.getLotNumber());
                actions.addAll(Set.of(Action.DELETE, Action.UPDATE, Action.OIL_QUALITY));
                actions.add(Action.GEN_PDF);
            }
            case OIL_CONTROLLED -> {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[mapOilDeliveryActions] Adding OIL_CONTROLLED status actions for oil delivery " + delivery.getLotNumber());
                actions.add(Action.GEN_PDF_QC_OIL);
                actions.add(Action.GEN_PDF);
                if (delivery.getOperationType() == OperationType.PAYMENT) {
                    actions.add(Action.COMPLETE_PAYMENT_DETAILS);
                } else {
                    actions.add(Action.SET_PRICE);
                }
            }
            case WAITING_FOR_PAYMENT_DETAILS -> {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[mapOilDeliveryActions] Adding WAITING_FOR_PAYMENT_DETAILS status actions for oil delivery " + delivery.getLotNumber());
                actions.add(Action.COMPLETE_PAYMENT_DETAILS);
                actions.add(Action.GEN_PDF);
            }
            case STOCK_READY -> {
                actions.add(Action.GEN_PDF);
                actions.add(Action.GEN_PDF_QC_OIL);
            }
            case COMPLETED, IN_STOCK -> {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[mapOilDeliveryActions] Adding GEN_PDF_BON_PROD status actions for oil delivery " + delivery.getLotNumber());
                actions.add(Action.GEN_PDF_PRODUCTION);
                actions.add(Action.GEN_PDF_QC_OIL);
                actions.add(Action.GEN_PDF);
                if (delivery.getOperationType() == OperationType.OIL_PURCHASE && !Boolean.TRUE.equals(delivery.getPaid())) {
                    actions.add(Action.PAY);
                    actions.add(Action.GEN_INVOICE);
                }
            }
        }

        OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[mapOilDeliveryActions] Final actions for oil delivery %s: %s", delivery.getLotNumber(), actions);
        OOSMLogger.logMethodExit(this.getClass(), "mapOilDeliveryActions", actions);
        OOSMLogger.logPerformance(this.getClass(), "mapOilDeliveryActions", startTime, System.currentTimeMillis());
        return actions;
    }

    /**
     * Creates oil reception records from olive deliveries based on operation type and payment flag.
     * This method handles different scenarios: exchange operations, base operations, olive purchases, and payment operations.
     *
     * @param uuid      The UUID of the original olive delivery
     * @param isPayment Boolean flag indicating if this is for payment purposes
     * @param std
     * @return The newly created oil reception delivery
     * @throws IllegalArgumentException if uuid is null or delivery is not found
     * @throws RuntimeException         if there's an error during processing
     */
    @Transactional
    public UnifiedDelivery createOilRecFromOliveRecImpl(UUID uuid, Boolean isPayment, String std) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "createOilRecFromOliveRecImpl", String.format("uuid=%s, isPayment=%s", uuid, isPayment));

        // Validate input parameters
        if (uuid == null) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[createOilRecFromOliveRecImpl] UUID is null");
            throw new IllegalArgumentException("UUID cannot be null");
        }

        if (isPayment == null) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[createOilRecFromOliveRecImpl] isPayment flag is null");
            throw new IllegalArgumentException("isPayment flag cannot be null");
        }

        try {
            // Find the original delivery
            UnifiedDelivery delivery = repository.findByIdAndIsDeletedFalse(uuid).orElseThrow(() -> {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[createOilRecFromOliveRecImpl] Original delivery not found with UUID: " + uuid);
                return new EntityNotFoundException("Original delivery not found: " + uuid);
            });

            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[createOilRecFromOliveRecImpl] Found original delivery %s (Type: %s, Operation: %s, Status: %s)", delivery.getLotNumber(), delivery.getDeliveryType(), delivery.getOperationType(), delivery.getStatus());

            // Validate delivery type
            if (delivery.getDeliveryType() != DeliveryType.OLIVE) {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[createOilRecFromOliveRecImpl] Invalid delivery type for oil reception creation: %s (expected OLIVE)", delivery.getDeliveryType());
                throw new IllegalArgumentException("Oil reception can only be created from OLIVE deliveries");
            }

            // Validate delivery status
            if (delivery.getStatus() == OliveLotStatus.IN_STOCK) {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.WARN, "[createOilRecFromOliveRecImpl] Creating oil reception for delivery %s that is already IN_STOCK", delivery.getLotNumber());
            }

            UnifiedDelivery oilDelivery = null;

            // Process based on operation type and payment flag
            if (delivery.getOperationType() == OperationType.EXCHANGE || delivery.getOperationType() == OperationType.BASE || delivery.getOperationType() == OperationType.OLIVE_PURCHASE) {

                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[createOilRecFromOliveRecImpl] Creating oil reception for %s operation", delivery.getOperationType());
                oilDelivery = creatOilRecForOtherOPS(delivery);

            } else if (isPayment) {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[createOilRecFromOliveRecImpl] Creating oil reception for payment purposes");
                oilDelivery = createOilRecForPayment(delivery, std);

            } else {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.WARN, "[createOilRecFromOliveRecImpl] No oil reception created for delivery %s (Operation: %s, isPayment: %s)", delivery.getLotNumber(), delivery.getOperationType(), isPayment);
                throw new IllegalArgumentException("No oil reception can be created for this operation type and payment flag combination");
            }

            if (oilDelivery != null) {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[createOilRecFromOliveRecImpl] Successfully created oil reception %s from olive delivery %s", oilDelivery.getLotNumber(), delivery.getLotNumber());
            }

            return oilDelivery;

        } catch (EntityNotFoundException e) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[createOilRecFromOliveRecImpl] Entity not found error: " + e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[createOilRecFromOliveRecImpl] Validation error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[createOilRecFromOliveRecImpl] Unexpected error during oil reception creation: " + e.getMessage(), e);
            throw new RuntimeException("Failed to create oil reception from olive delivery", e);
        } finally {
            OOSMLogger.logMethodExit(this.getClass(), "createOilRecFromOliveRecImpl", null);
            OOSMLogger.logPerformance(this.getClass(), "createOilRecFromOliveRecImpl", startTime, System.currentTimeMillis());
        }
    }

    /**
     * Creates an oil reception record for operations other than payment (EXCHANGE, BASE, OLIVE_PURCHASE).
     * This method creates a new oil delivery linked to the original olive delivery.
     *
     * @param delivery The original olive delivery
     * @return The newly created oil reception delivery
     * @throws IllegalArgumentException if delivery is null or invalid
     * @throws RuntimeException         if there's an error during processing
     */
    private UnifiedDelivery creatOilRecForOtherOPS(UnifiedDelivery delivery) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "creatOilRecForOtherOPS", delivery);

        if (delivery == null) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[creatOilRecForOtherOPS] Original delivery is null");
            throw new IllegalArgumentException("Original delivery cannot be null");
        }

        OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[creatOilRecForOtherOPS] Creating oil reception for %s operation from olive delivery %s", delivery.getOperationType(), delivery.getLotNumber());

        try {
            UnifiedDelivery newDelivery = new UnifiedDelivery();
            newDelivery.setDeliveryType(DeliveryType.OIL);
            newDelivery.setStatus(OliveLotStatus.NEW);
            newDelivery.setLotNumber(delivery.getLotNumber());
            newDelivery.setOliveQuantity(delivery.getPoidsNet());
            newDelivery.setDeliveryNumber(delivery.getDeliveryNumber());
            newDelivery.setDeliveryDate(LocalDateTime.now());

            // Set oil quantity with null safety
            Double oilQuantity = delivery.getOilQuantity();
            if (oilQuantity == null || oilQuantity <= 0) {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.WARN, "[creatOilRecForOtherOPS] Invalid oil quantity for delivery %s: %s, setting to 0.0", delivery.getLotNumber(), oilQuantity);
                oilQuantity = 0.0;
            }
            newDelivery.setOilQuantity(oilQuantity);
            newDelivery.setPoidsNet(oilQuantity);

            // Initialize pricing
            newDelivery.setUnitPrice(0.0);

            // Copy relevant information from original delivery
            newDelivery.setOilType(delivery.getOliveType());
            newDelivery.setOliveQuantity(delivery.getPoidsNet());
            newDelivery.setOliveType(delivery.getOliveType());
            newDelivery.setRegion(delivery.getRegion());
            newDelivery.setSupplier(delivery.getSupplier());
            newDelivery.setLotOliveNumber(delivery.getLotNumber()); // Link to original olive delivery
            newDelivery.setOperationType(delivery.getOperationType());
            newDelivery.setOilVariety(delivery.getOliveVariety());

            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[creatOilRecForOtherOPS] Created oil reception with operation type INTERNAL_RECEPTION, linked to olive lot: %s", delivery.getLotNumber());

            // Save both deliveries to maintain consistency
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[creatOilRecForOtherOPS] Saving original olive delivery to maintain consistency");
            repository.save(delivery);

            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[creatOilRecForOtherOPS] Saving new oil reception");
            UnifiedDelivery savedOilDelivery = repository.save(newDelivery);

            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[creatOilRecForOtherOPS] Successfully created oil reception %s from olive delivery %s", savedOilDelivery.getLotNumber(), delivery.getLotNumber());

            return savedOilDelivery;

        } catch (Exception e) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[creatOilRecForOtherOPS] Error creating oil reception for other operations: " + e.getMessage(), e);
            throw new RuntimeException("Failed to create oil reception for other operations", e);
        } finally {
            OOSMLogger.logMethodExit(this.getClass(), "creatOilRecForOtherOPS", null);
            OOSMLogger.logPerformance(this.getClass(), "creatOilRecForOtherOPS", startTime, System.currentTimeMillis());
        }
    }

    /**
     * Creates an oil reception record specifically for payment purposes.
     * This method is called when an olive delivery needs to be paid in oil.
     *
     * @param delivery The original olive delivery that needs oil payment
     * @return The newly created oil reception delivery
     * @throws IllegalArgumentException if delivery is null or invalid
     * @throws RuntimeException         if there's an error during processing
     */
    @Transactional
    protected UnifiedDelivery createOilRecForPayment(UnifiedDelivery delivery, String std) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "createOilRecForPayment", delivery);

        if (delivery == null) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[createOilRecForPayment] Original delivery is null");
            throw new IllegalArgumentException("Original delivery cannot be null");
        }

        // Validate delivery type
        if (delivery.getDeliveryType() != DeliveryType.OLIVE) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[createOilRecForPayment] Invalid delivery type for payment oil reception: %s (expected OLIVE)", delivery.getDeliveryType());
            throw new IllegalArgumentException("Payment oil reception can only be created from OLIVE deliveries");
        }

        // Validate supplier
        if (delivery.getSupplier() == null) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[createOilRecForPayment] Cannot create payment oil reception for delivery without supplier");
            throw new IllegalArgumentException("Delivery must have a supplier for payment oil reception");
        }

        OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[createOilRecForPayment] Creating oil reception for payment from olive delivery %s (Supplier: %s)", delivery.getLotNumber(), delivery.getSupplier().getName());

        try {

            UnifiedDelivery newDelivery = new UnifiedDelivery();
            if (std != null && !std.isEmpty()) {
                Optional<StorageUnitDto> stdModel = storageUnitRepo.findById(UUID.fromString(std)).map((element) -> modelMapper.map(element, StorageUnitDto.class));
                newDelivery.setStorageUnit(modelMapper.map(stdModel.get(), StorageUnit.class));
            }
            Optional<UnifiedDelivery> existingPaymentLeg = deliveryRepository
                    .findAllByLotNumberAndDeliveryTypeAndIsDeletedFalse(delivery.getLotNumber(), DeliveryType.OIL)
                    .stream()
                    .filter(d -> d.getOperationType() == OperationType.PAYMENT)
                    .filter(d -> d.getStatus() != OliveLotStatus.CANCELLED)
                    .findFirst();
            if (existingPaymentLeg.isPresent()) {
                UnifiedDelivery existing = existingPaymentLeg.get();
                if (existing.isHasQualityControl() || existing.getStatus() == OliveLotStatus.STOCK_READY) {
                    throw new IllegalArgumentException("Payment oil reception already exists for olive lot " + delivery.getLotNumber());
                }
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO,
                        "[createOilRecForPayment] Reusing existing payment oil reception %s for olive lot %s",
                        existing.getLotNumber(), delivery.getLotNumber());
                applyStorageUnitToPaymentLeg(existing, std);
                return existing;
            }

            newDelivery.setDeliveryType(DeliveryType.OIL);
            newDelivery.setStatus(OliveLotStatus.NEW);
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[createOilRecForPayment] used the original olive reception lot number as new lot number: %s, delivery number: %s", delivery.getLotNumber(), delivery.getDeliveryNumber());

            // Set basic delivery information
            newDelivery.setLotNumber(delivery.getLotNumber());
            newDelivery.setDeliveryNumber(delivery.getDeliveryNumber());
            newDelivery.setDeliveryDate(LocalDateTime.now());

            // Initialize payment-related fields
            newDelivery.setOilQuantity(delivery.getOilQuantity()); // Will be set during payment processing
            newDelivery.setUnitPrice(0.0);   // Will be set during payment processing

            // Copy relevant information from original delivery
            newDelivery.setOilType(delivery.getOliveType());
            newDelivery.setOliveType(delivery.getOliveType());
            newDelivery.setRegion(delivery.getRegion());
            newDelivery.setSupplier(delivery.getSupplier());
            newDelivery.setLotOliveNumber(delivery.getLotNumber()); // Link to original olive delivery
            newDelivery.setOperationType(OperationType.PAYMENT);
            newDelivery.setParcel(delivery.getParcel());
            newDelivery.setOilVariety(delivery.getOliveVariety());
            newDelivery.setOliveVariety(delivery.getOliveVariety());

            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[createOilRecForPayment] Created oil reception with operation type PAYMENT, linked to olive lot: %s", delivery.getLotNumber());

            // Save both deliveries to maintain consistency
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[createOilRecForPayment] Saving original olive delivery to maintain consistency");
            repository.save(delivery);

            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[createOilRecForPayment] Saving new oil reception for payment");
            UnifiedDelivery savedOilDelivery = repository.save(newDelivery);

            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[createOilRecForPayment] Successfully created oil reception %s for payment from olive delivery %s", savedOilDelivery.getLotNumber(), delivery.getLotNumber());

            return savedOilDelivery;

        } catch (Exception e) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[createOilRecForPayment] Error creating oil reception for payment: " + e.getMessage(), e);
            throw new RuntimeException("Failed to create oil reception for payment", e);
        } finally {
            OOSMLogger.logMethodExit(this.getClass(), "createOilRecForPayment", null);
            OOSMLogger.logPerformance(this.getClass(), "createOilRecForPayment", startTime, System.currentTimeMillis());
        }
    }

    private void applyStorageUnitToPaymentLeg(UnifiedDelivery oilDelivery, String std) {
        if (std == null || std.isEmpty()) {
            return;
        }
        storageUnitRepo.findById(UUID.fromString(std))
                .map(unit -> modelMapper.map(unit, StorageUnit.class))
                .ifPresent(oilDelivery::setStorageUnit);
        deliveryRepository.save(oilDelivery);
    }

    private boolean hasActivePaymentOilLeg(UnifiedDelivery oliveDelivery) {
        if (oliveDelivery == null || oliveDelivery.getLotNumber() == null) {
            return false;
        }
        return deliveryRepository
                .findAllByLotNumberAndDeliveryTypeAndIsDeletedFalse(oliveDelivery.getLotNumber(), DeliveryType.OIL)
                .stream()
                .anyMatch(d -> d.getOperationType() == OperationType.PAYMENT
                        && d.getStatus() != OliveLotStatus.CANCELLED);
    }


    private int parseDeliveryNumber(String deliveryNumber) {
        if (deliveryNumber == null || deliveryNumber.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(deliveryNumber.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int resolveNextDeliverySequence() {
        Set<Integer> used = deliveryRepository.findAllDeliveryNumbers().stream()
                .map(this::parseDeliveryNumber)
                .filter(n -> n > 0)
                .collect(Collectors.toSet());
        int seq = 1;
        while (used.contains(seq)) {
            seq++;
        }
        return seq;
    }

    private String buildLotNumber(UnifiedDelivery delivery, int seq) {
        Olive_Oil_Type type = delivery.getDeliveryType() == DeliveryType.OIL
                ? delivery.getOilType()
                : delivery.getOliveType();
        if (type == null) {
            return "";
        }
        LocalDateTime date = delivery.getDeliveryDate() != null ? delivery.getDeliveryDate() : LocalDateTime.now();
        String yearPart = String.format(D1, date.getYear() % 100);
        return String.format("%04d", seq) + type.name() + yearPart;
    }

    private void assignNumbersOnCreate(UnifiedDelivery delivery) {
        int seq = resolveNextDeliverySequence();
        delivery.setDeliveryNumber(String.valueOf(seq));
        delivery.setLotNumber(buildLotNumber(delivery, seq));
    }

    @Transactional(readOnly = true)
    public NextDeliveryNumbersDto previewNextNumbers(DeliveryType deliveryType, Olive_Oil_Type oliveType, Olive_Oil_Type oilType) {
        int seq = resolveNextDeliverySequence();
        UnifiedDelivery preview = new UnifiedDelivery();
        preview.setDeliveryType(deliveryType);
        preview.setDeliveryDate(LocalDateTime.now());
        if (deliveryType == DeliveryType.OLIVE) {
            preview.setOliveType(oliveType);
        } else if (deliveryType == DeliveryType.OIL) {
            preview.setOilType(oilType);
        }
        return new NextDeliveryNumbersDto(String.valueOf(seq), buildLotNumber(preview, seq));
    }

    /**
     * Updates the status of a delivery.
     * This method validates the delivery exists and updates its status with proper logging.
     *
     * @param id     The delivery ID to update
     * @param status The new status to set
     * @throws IllegalArgumentException if id or status is null
     * @throws EntityNotFoundException  if delivery is not found
     * @throws RuntimeException         if there's an error during processing
     */
    @Transactional
    public void updateStatus(UUID id, OliveLotStatus status, String cause) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "updateStatus", String.format("id=%s, status=%s", id, status));

        // Validate input parameters
        if (id == null) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updateStatus] Delivery ID is null");
            throw new IllegalArgumentException("Delivery ID cannot be null");
        }

        if (status == null) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updateStatus] Status is null");
            throw new IllegalArgumentException("Status cannot be null");
        }

        try {
            // Find the delivery
            UnifiedDelivery delivery = repository.findByIdAndIsDeletedFalse(id).orElseThrow(() -> {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updateStatus] Delivery not found with ID: " + id);
                return new EntityNotFoundException("Delivery not found: " + id);
            });
            if (cause != null) {
                delivery.setDescription(cause);
            }
            OliveLotStatus oldStatus = delivery.getStatus();
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[updateStatus] Found delivery %s (Type: %s, Old Status: %s, New Status: %s)", delivery.getLotNumber(), delivery.getDeliveryType(), oldStatus, status);

            // Validate status transition
            if (oldStatus == status) {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.WARN, "[updateStatus] Delivery %s status is already %s, no update needed", delivery.getLotNumber(), status);
                return;
            }

            // Update status
            delivery.setStatus(status);
            UnifiedDelivery savedDelivery = deliveryRepository.save(delivery);

            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[updateStatus] Successfully updated delivery %s status from %s to %s", savedDelivery.getLotNumber(), oldStatus, savedDelivery.getStatus());

        } catch (EntityNotFoundException e) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updateStatus] Entity not found error: " + e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updateStatus] Validation error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updateStatus] Unexpected error during status update: " + e.getMessage(), e);
            throw new RuntimeException("Failed to update delivery status", e);
        }

        OOSMLogger.logMethodExit(this.getClass(), "updateStatus", null);
        OOSMLogger.logPerformance(this.getClass(), "updateStatus", startTime, System.currentTimeMillis());
    }

    /**
     * Updates unit price and calculates total price for deliveries.
     * This method handles both OIL and OLIVE delivery types with appropriate status updates.
     *
     * @param id        The delivery ID to update
     * @param unitPrice The new unit price (must be positive)
     * @throws IllegalArgumentException if unitPrice is null or negative
     * @throws EntityNotFoundException  if delivery is not found
     * @throws RuntimeException         if there's an error during processing
     */
    @Transactional
    public void updateprice(UUID id, Double unitPrice) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "updateprice", String.format("id=%s, unitPrice=%.2f", id, unitPrice));

        // Validate input parameters
        if (id == null) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updateprice] Delivery ID is null");
            throw new IllegalArgumentException("Delivery ID cannot be null");
        }

        if (unitPrice == null) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updateprice] Unit price is null");
            throw new IllegalArgumentException("Unit price cannot be null");
        }

        if (unitPrice <= 0) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updateprice] Unit price must be positive: " + unitPrice);
            throw new IllegalArgumentException("Unit price must be positive");
        }

        try {
            // Find the delivery
            UnifiedDelivery delivery = repository.findByIdAndIsDeletedFalse(id).orElseThrow(() -> {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updateprice] Delivery not found with ID: " + id);
                return new EntityNotFoundException("Delivery not found: " + id);
            });

            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[updateprice] Found delivery %s (Type: %s, Status: %s)", delivery.getLotNumber(), delivery.getDeliveryType(), delivery.getStatus());

            // Validate delivery state
            if (delivery.getStatus() == OliveLotStatus.IN_STOCK) {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.WARN, "[updateprice] Updating price for delivery %s that is already IN_STOCK", delivery.getLotNumber());
            }

            // Update unit price
            delivery.setUnitPrice(unitPrice);

            // Process based on delivery type
            switch (delivery.getDeliveryType()) {
                case OIL -> {
                    if (delivery.getOilQuantity() == null || delivery.getOilQuantity() <= 0) {
                        OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updateprice] Invalid oil quantity for delivery %s: %s", delivery.getLotNumber(), delivery.getOilQuantity());
                        throw new IllegalArgumentException("Oil quantity must be positive for OIL deliveries");
                    }
                    double totalPrice = unitPrice * delivery.getOilQuantity();
                    delivery.setPrice(totalPrice);
                    delivery.setStatus(OliveLotStatus.IN_STOCK);
                    delivery.setUnpaidAmount(totalPrice);
                    OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[updateprice] Updated OIL delivery %s: unitPrice=%.2f, oilQuantity=%.2f, totalPrice=%.2f", delivery.getLotNumber(), unitPrice, delivery.getOilQuantity(), totalPrice);

                    // Create oil transaction
                    OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[updateprice] Creating oil transaction for delivery " + delivery.getLotNumber());
                    oilTransactionService.createSingleOilTransactionIn(delivery);
                    // Cash/obligation settlements are recorded on processPayment — not at pricing time.
                }
                case OLIVE -> {
                    if (delivery.getPoidsNet() == null || delivery.getPoidsNet() <= 0) {
                        OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updateprice] Invalid poids net for delivery %s: %s", delivery.getLotNumber(), delivery.getPoidsNet());
                        throw new IllegalArgumentException("Poids net must be positive for OLIVE deliveries");
                    }

                    double totalPrice = unitPrice * delivery.getPoidsNet();
                    delivery.setPrice(totalPrice);
                    delivery.setStatus(OliveLotStatus.PROD_READY);
                    delivery.setUnpaidAmount(totalPrice);

                    OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[updateprice] Updated OLIVE delivery %s: unitPrice=%.2f, poidsNet=%.2f, totalPrice=%.2f", delivery.getLotNumber(), unitPrice, delivery.getPoidsNet(), totalPrice);
                }
                case null, default -> {
                    OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updateprice] Unsupported delivery type for delivery %s: %s", delivery.getLotNumber(), delivery.getDeliveryType());
                    throw new IllegalArgumentException("Unsupported delivery type: " + delivery.getDeliveryType());
                }
            }

            // Save the updated delivery
            UnifiedDelivery savedDelivery = deliveryRepository.save(delivery);
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[updateprice] Successfully saved delivery %s with new status: %s", savedDelivery.getLotNumber(), savedDelivery.getStatus());

        } catch (EntityNotFoundException e) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updateprice] Entity not found error: " + e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updateprice] Validation error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updateprice] Unexpected error during price update: " + e.getMessage(), e);
            throw new RuntimeException("Failed to update delivery price", e);
        }

        OOSMLogger.logMethodExit(this.getClass(), "updateprice", null);
        OOSMLogger.logPerformance(this.getClass(), "updateprice", startTime, System.currentTimeMillis());
    }

    /**
     * Updates pricing for payment reception and creates oil transaction.
     * This method is called when payment details are completed for oil receptions.
     *
     * @param dto The exchange pricing data containing delivery ID, unit price, and total price
     * @throws EntityNotFoundException  if delivery is not found
     * @throws IllegalArgumentException if pricing data is invalid
     */
    @Transactional
    public void updatePrincingForPaymentreception(ExchangePricingDto dto) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "updatePrincingForPaymentreception", dto);

        // Validate input parameters
        if (dto == null) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updatePrincingForPaymentreception] ExchangePricingDto is null");
            throw new IllegalArgumentException("ExchangePricingDto cannot be null");
        }

        if (dto.getDeliveryId() == null) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updatePrincingForPaymentreception] Delivery ID is null");
            throw new IllegalArgumentException("Delivery ID cannot be null");
        }

        OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[updatePrincingForPaymentreception] Processing payment reception for delivery %s with unit price %.2f and total price %.2f", dto.getDeliveryId(), dto.getUnitPrice(), dto.getPrice());

        try {
            // Find the oilDelivery
            UnifiedDelivery oilDelivery = deliveryRepository.findById(dto.getDeliveryId()).orElseThrow(() -> {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updatePrincingForPaymentreception] Delivery not found with ID: " + dto.getDeliveryId());
                return new EntityNotFoundException("Delivery not found: " + dto.getDeliveryId());
            });
            UnifiedDelivery originalOliveDelivery = this.deliveryRepository.findByLotNumberAndDeliveryType(oilDelivery.getLotOliveNumber(), DeliveryType.OLIVE);

            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[updatePrincingForPaymentreception] Found oilDelivery %s (Status: %s, Type: %s)", oilDelivery.getLotNumber(), oilDelivery.getStatus(), oilDelivery.getDeliveryType());

            // Validate oilDelivery type
            if (oilDelivery.getDeliveryType() != DeliveryType.OIL) {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updatePrincingForPaymentreception] Invalid oilDelivery type for payment reception: %s (expected OIL)", oilDelivery.getDeliveryType());
                throw new IllegalArgumentException("Payment reception can only be processed for OIL deliveries");
            }

            if (oilDelivery.getOperationType() != OperationType.PAYMENT) {
                throw new IllegalArgumentException("Payment pricing can only be processed for PAYMENT operation deliveries");
            }

            if (oilDelivery.getStatus() != OliveLotStatus.OIL_CONTROLLED
                    && oilDelivery.getStatus() != OliveLotStatus.WAITING_FOR_PAYMENT_DETAILS) {
                throw new IllegalArgumentException("Payment pricing requires oil delivery status OIL_CONTROLLED, current: " + oilDelivery.getStatus());
            }

            if (originalOliveDelivery == null) {
                throw new EntityNotFoundException("Linked olive delivery not found for lot " + oilDelivery.getLotOliveNumber());
            }

            if (originalOliveDelivery.getOperationType() != OperationType.SIMPLE_RECEPTION) {
                throw new IllegalArgumentException("Payment oil leg must link to a SIMPLE_RECEPTION olive delivery");
            }

            // Validate pricing data
            if (dto.getUnitPrice() == null || dto.getUnitPrice() <= 0) {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updatePrincingForPaymentreception] Invalid unit price: " + dto.getUnitPrice());
                throw new IllegalArgumentException("Unit price must be positive");
            }

            if (dto.getPrice() == null || dto.getPrice() <= 0) {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updatePrincingForPaymentreception] Invalid total price: " + dto.getPrice());
                throw new IllegalArgumentException("Total price must be positive");
            }

            double paymentAmount = r3(dto.getPrice());
            double remainingUnpaid = r3(Math.max(0.0, safe(originalOliveDelivery.getUnpaidAmount())));
            if (paymentAmount > remainingUnpaid + 0.001) {
                throw new IllegalArgumentException("Payment amount exceeds remaining unpaid balance");
            }

            // Update pricing on the oil leg
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[updatePrincingForPaymentreception] Updating pricing for oilDelivery %s: unitPrice=%.2f, price=%.2f", oilDelivery.getLotNumber(), dto.getUnitPrice(), dto.getPrice());

            oilDelivery.setUnitPrice(dto.getUnitPrice());
            oilDelivery.setOilQuantity(dto.getOilQuantity());
            oilDelivery.setPoidsNet(dto.getOilQuantity());
            oilDelivery.setPrice(paymentAmount);
            oilDelivery.setPaidAmount(paymentAmount);
            oilDelivery.setUnpaidAmount(0.0);
            oilDelivery.setPaid(true);
            if (dto.getQualityGrade() != null && !dto.getQualityGrade().isBlank()) {
                oilDelivery.setCategoryOliveOil(dto.getQualityGrade());
            }
            oilDelivery.setStatus(OliveLotStatus.STOCK_READY);

            double appliedOnOlive = updateDelivery(originalOliveDelivery, paymentAmount);

            // Save the updated oilDelivery
            UnifiedDelivery savedDelivery = deliveryRepository.save(oilDelivery);
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[updatePrincingForPaymentreception] Successfully saved oilDelivery %s with new status: %s", savedDelivery.getLotNumber(), savedDelivery.getStatus());

            // Create oil transaction
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[updatePrincingForPaymentreception] Creating oil transaction for oilDelivery " + oilDelivery.getLotNumber());
            oilTransactionService.createSingleOilTransactionIn(savedDelivery);

            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setAmount(appliedOnOlive);
            paymentDTO.setCurrency(Currency.TND);
            paymentDTO.setPaymentMethod(PaymentMethod.OIL);
            if (originalOliveDelivery.getSupplier() != null) {
                paymentDTO.setSupplier(modelMapper.map(originalOliveDelivery.getSupplier(), SupplierDto.class));
            }
            prepareFinanacalTransaction(paymentDTO, appliedOnOlive, originalOliveDelivery, TransactionDirection.INBOUND, TransactionType.PAYMENT, OperationType.SIMPLE_RECEPTION);

            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[updatePrincingForPaymentreception] Successfully completed payment reception processing for oilDelivery %s", oilDelivery.getLotNumber());

        } catch (EntityNotFoundException e) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updatePrincingForPaymentreception] Entity not found error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updatePrincingForPaymentreception] Unexpected error during payment reception processing: " + e.getMessage(), e);
            throw new RuntimeException("Failed to process payment reception", e);
        }

        OOSMLogger.logMethodExit(this.getClass(), "updatePrincingForPaymentreception", null);
        OOSMLogger.logPerformance(this.getClass(), "updatePrincingForPaymentreception", startTime, System.currentTimeMillis());
    }

    /**
     * Updates exchange pricing and creates oil transaction out.
     * This method is used for exchange operations where oil is being transferred out.
     *
     * @param dto The exchange pricing data containing delivery ID, unit price, and total price
     * @throws EntityNotFoundException  if delivery is not found
     * @throws IllegalArgumentException if pricing data is invalid
     */
    @Transactional
    public void updateExchangePricingAndCreateOilTransactionOut(ExchangePricingDto dto) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "updateExchangePricingAndCreateOilTransactionOut", dto);

        // Validate input parameters
        if (dto == null) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updateExchangePricingAndCreateOilTransactionOut] ExchangePricingDto is null");
            throw new IllegalArgumentException("ExchangePricingDto cannot be null");
        }

        if (dto.getDeliveryId() == null) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updateExchangePricingAndCreateOilTransactionOut] Delivery ID is null");
            throw new IllegalArgumentException("Delivery ID cannot be null");
        }

        OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[updateExchangePricingAndCreateOilTransactionOut] Processing exchange pricing for delivery %s with unit price %.2f and total price %.2f", dto.getDeliveryId(), dto.getUnitPrice(), dto.getPrice());

        try {
            // Find the delivery
            UnifiedDelivery delivery = deliveryRepository.findById(dto.getDeliveryId()).orElseThrow(() -> {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updateExchangePricingAndCreateOilTransactionOut] Delivery not found with ID: " + dto.getDeliveryId());
                return new EntityNotFoundException("Delivery not found: " + dto.getDeliveryId());
            });

            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[updateExchangePricingAndCreateOilTransactionOut] Found delivery %s (Status: %s, Type: %s, Operation: %s)", delivery.getLotNumber(), delivery.getStatus(), delivery.getDeliveryType(), delivery.getOperationType());


            // Validate operation type for exchange
            if (delivery.getOperationType() != OperationType.EXCHANGE) {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.WARN, "[updateExchangePricingAndCreateOilTransactionOut] Processing exchange pricing for non-exchange operation: %s", delivery.getOperationType());
            }

            // Validate pricing data
            if (dto.getUnitPrice() == null || dto.getUnitPrice() <= 0) {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updateExchangePricingAndCreateOilTransactionOut] Invalid unit price: " + dto.getUnitPrice());
                throw new IllegalArgumentException("Unit price must be positive");
            }

            if (dto.getPrice() == null || dto.getPrice() <= 0) {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updateExchangePricingAndCreateOilTransactionOut] Invalid total price: " + dto.getPrice());
                throw new IllegalArgumentException("Total price must be positive");
            }

            // Update pricing on the Delivery
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[updateExchangePricingAndCreateOilTransactionOut] Updating exchange pricing for delivery %s: unitPrice=%.2f, price=%.2f", delivery.getLotNumber(), dto.getUnitPrice(), dto.getPrice());

            delivery.setUnitPrice(dto.getUnitPrice());
            delivery.setPrice(dto.getPrice());
            delivery.setUnpaidAmount(dto.getPrice());
            delivery.setStatus(OliveLotStatus.PROD_READY);


            // TODO: Uncomment when quality grade is implemented
            // delivery.setQualityGrade(dto.getQualityGrade());

            // Save the updated delivery
            UnifiedDelivery savedDelivery = deliveryRepository.save(delivery);
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[updateExchangePricingAndCreateOilTransactionOut] Successfully saved delivery %s with new status: %s", savedDelivery.getLotNumber(), savedDelivery.getStatus());

            // Create oil transaction out
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[updateExchangePricingAndCreateOilTransactionOut] Creating oil transaction out for delivery " + delivery.getLotNumber());
            oilTransactionService.createSingleOilTransactionOut(savedDelivery, dto);

            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.INFO, "[updateExchangePricingAndCreateOilTransactionOut] Successfully completed exchange pricing processing for delivery %s", delivery.getLotNumber());

        } catch (EntityNotFoundException e) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updateExchangePricingAndCreateOilTransactionOut] Entity not found error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.ERROR, "[updateExchangePricingAndCreateOilTransactionOut] Unexpected error during exchange pricing processing: " + e.getMessage(), e);
            throw new RuntimeException("Failed to process exchange pricing", e);
        }

        OOSMLogger.logMethodExit(this.getClass(), "updateExchangePricingAndCreateOilTransactionOut", null);
        OOSMLogger.logPerformance(this.getClass(), "updateExchangePricingAndCreateOilTransactionOut", startTime, System.currentTimeMillis());
    }

    @Transactional(readOnly = true)
    public UnifiedDeliveryDTO getByOliveLotNumber(UUID id) {
        var t = deliveryRepository.findByLotOliveNumber(id);
        if (Objects.isNull(t)) {
            return null;
        }
        return mapToDto(t);
    }

    @Transactional(readOnly = true)
    public UnifiedDeliveryDTO getByLotNumber(String lotNumber) {
        var t = deliveryRepository.findByLotNumberAndDeliveryType(lotNumber, DeliveryType.OLIVE);
        return mapToDto(t);
    }

    @Transactional
    public void processPayment(PaymentDTO paymentDTO) {
        if (paymentDTO.getIdOperation() == null) {
            throw new IllegalArgumentException("Payment operation id is required");
        }
        double amount = paymentDTO.getAmount() != null ? paymentDTO.getAmount() : 0d;
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than 0");
        }

        UnifiedDelivery delivery = deliveryRepository.findByIdAndIsDeletedFalse(paymentDTO.getIdOperation()).orElse(null);

        if (delivery == null) {
            throw new IllegalArgumentException("Delivery not found for ID: " + paymentDTO.getIdOperation());
        }
        double applied = updateDelivery(delivery, amount);
        if (applied <= 0) {
            throw new IllegalArgumentException("No payable balance remaining for delivery: " + delivery.getId());
        }

        switch (delivery.getOperationType()) {
            case OIL_PURCHASE ->
                    prepareFinanacalTransaction(paymentDTO, applied, delivery, TransactionDirection.OUTBOUND, TransactionType.PURCHASE, OperationType.OIL_PURCHASE);
            case OLIVE_PURCHASE ->
                    prepareFinanacalTransaction(paymentDTO, applied, delivery, TransactionDirection.OUTBOUND, TransactionType.PURCHASE, OperationType.OLIVE_PURCHASE);
            case BASE ->
                    prepareFinanacalTransaction(paymentDTO, applied, delivery, TransactionDirection.OUTBOUND, TransactionType.PURCHASE, OperationType.BASE);
            case SIMPLE_RECEPTION ->
                    prepareFinanacalTransaction(paymentDTO, applied, delivery, TransactionDirection.INBOUND, TransactionType.PAYMENT, OperationType.SIMPLE_RECEPTION);
            case PAYMENT ->
                    prepareFinanacalTransaction(paymentDTO, applied, delivery, TransactionDirection.OUTBOUND, TransactionType.PURCHASE, OperationType.PAYMENT);
            default ->
                    throw new IllegalArgumentException("Unsupported operation type for payment: " + delivery.getOperationType());
        }

    }

    /**
     * Applies a payment/refund delta to the delivery balance.
     * @return the clamped amount actually applied (positive = payment, negative = refund)
     */
    @Transactional
    public double updateDelivery(UnifiedDelivery d, double amountDelta) {
        // 1) Resolve total (use stored total; fallback to unit * qty if missing)
        double total = r3(safe(d.getPrice()));
        if (total <= 0.0) {
            total = r3(safe(d.getUnitPrice()) * safe(d.getOilQuantity())); // remove this block if you never want fallback
        }

        // 2) Current paid/unpaid
        double paid = r3(safe(d.getPaidAmount()));
        paid = clamp(paid, 0.0, total);
        double unpaid = r3(Math.max(0.0, total - paid));

        // 3) Normalize delta: cap overpay/refund
        double change = r3(amountDelta);
        if (change >= 0.0) {
            change = Math.min(change, unpaid);          // cannot overpay
        } else {
            change = -Math.min(Math.abs(change), paid); // cannot refund more than paid
        }

        // 4) Apply & round
        double newPaid = r3(clamp(paid + change, 0.0, total));
        double newUnpaid = r3(Math.max(0.0, total - newPaid));
        boolean fullyPaid = (newUnpaid == 0.0); // safe since rounded to 3

        // 5) Persist
        d.setPrice(r3(total));
        d.setPaidAmount(newPaid);
        d.setUnpaidAmount(newUnpaid);
        d.setPaid(fullyPaid);

        deliveryRepository.save(d);
        return change;
    }

    private void prepareFinanacalTransaction(PaymentDTO paymentDTO, double amount, UnifiedDelivery delivery, TransactionDirection direction, TransactionType transactionType, OperationType operationType) {
        if (amount <= 0) {
            return;
        }
        FinancialTransactionDto financialTransactionDto = new FinancialTransactionDto();
        financialTransactionDto.setTransactionType(transactionType);
        financialTransactionDto.setDirection(direction);
        financialTransactionDto.setAmount(BigDecimal.valueOf(amount));
        financialTransactionDto.setCurrency(paymentDTO.getCurrency() != null ? paymentDTO.getCurrency() : Currency.TND);
        financialTransactionDto.setPaymentMethod(paymentDTO.getPaymentMethod() != null ? paymentDTO.getPaymentMethod() : PaymentMethod.CASH);
        financialTransactionDto.setBankAccount(paymentDTO.getBankAccount() != null ? paymentDTO.getBankAccount() : null);
        financialTransactionDto.setCheckNumber(paymentDTO.getCheckNumber() != null ? paymentDTO.getCheckNumber() : null);
        financialTransactionDto.setLotNumber(delivery.getLotNumber());
        financialTransactionDto.setsupplier((paymentDTO.getSupplier() != null) ? modelMapper.map(paymentDTO.getSupplier(), com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto.class) : (delivery.getSupplier() != null ? modelMapper.map(delivery.getSupplier(), com.xdev.ooms.sharedkernel.communicator.models.shared.SupplierDto.class) : null));
        financialTransactionDto.setTransactionDate(LocalDateTime.now());
        financialTransactionDto.setApproved(true);
        financialTransactionDto.setApprovalDate(LocalDateTime.now());
        financialTransactionDto.setOperationType(operationType);
        financialTransactionDto.setExternalTransactionId(delivery.getId().toString());
        financialTransactionDto.setResourceName(ResourceName.UnifiedDelivery);
        if (delivery.getInvoiceNumber() != null && !delivery.getInvoiceNumber().isBlank()) {
            financialTransactionDto.setInvoiceReference(delivery.getInvoiceNumber());
        }
        financialTransactionDto.setDescription(buildDeliveryPaymentDescription(delivery, operationType));
        financialTransactionDto.setSyncProductionState(false);
        financialTransactionPort.record(financialTransactionDto);
    }

    private String buildDeliveryPaymentDescription(UnifiedDelivery delivery, OperationType operationType) {
        String lot = delivery.getLotNumber() != null ? delivery.getLotNumber() : delivery.getId().toString();
        String op = operationType != null ? operationType.name() : "PAYMENT";
        return "Paiement livraison (" + lot + ") - " + op;
    }

    @Override
    @Transactional
    public UnifiedDeliveryDTO delete(UUID id) {
        OOSMLogger.logMethodEntry(this.getClass(), "delete", id);
        try {
            if (id == null) {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.WARN, "Delete ID is null: {}", id);
                return null;
            }
            UnifiedDelivery entity = repository.findByIdAndIsDeletedFalse(id).orElse(null);
            if (entity == null) {
                OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.WARN, "Entity with ID {} not found for deletion", id);
                return null;
            }
            financialTransactionPort.reverseLinked(entity.getId().toString(), ResourceName.UnifiedDelivery);
            entity.setDeleted(true);
            UnifiedDelivery updatedEntity = repository.save(entity);
            Set<QualityControlResult> controlResults = entity.getQualityControlResults().stream().map(qc -> {
                QualityControlResult result = qualityControlResultRepository.findByIdAndIsDeletedFalse(qc.getId()).orElse(null);
                if (result != null) {
                    result.setDeleted(true);
                    return result;
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toSet());
            qualityControlResultRepository.saveAll(controlResults);
            return mapToDto(updatedEntity);
        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(), "Error deleting entity with ID: " + id, e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public UnifiedDeliveryDTO getByLotNumberAndType(String lotNumber, DeliveryType deliveryType) {

        try {
            // Make enum lookup case-insensitive and trim spaces

            var matches = deliveryRepository.findAllByLotNumberAndDeliveryTypeAndIsDeletedFalse(lotNumber, deliveryType);
            if (matches == null || matches.isEmpty()) {
                return null;
            }

            return mapToDto(matches.get(0));

        } catch (Exception e) {
            OOSMLogger.logException(this.getClass(), "Error getByLotNumberAndType  : ", e);
            return null;
        }
    }

    @Transactional(readOnly = true)
    public List<UnifiedDeliveryDTO> getDeliveriesByGlobalLotNumber(String lotNumber) {
        List<UnifiedDelivery> deliveries = deliveryRepository.findByGlobalLotNumberAndDeliveryTypeAndIsDeletedFalse(lotNumber, DeliveryType.OLIVE);
        List<UnifiedDeliveryDTO> result = new ArrayList<>(deliveries.size());
        deliveries.forEach(d -> result.add(mapToDto(d)));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public UnifiedDeliveryDTO findById(UUID id) {
        UnifiedDelivery entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found with this id " + id));
        return mapToDto(entity);
    }

    @Transactional(readOnly = true)
    protected UnifiedDeliveryDTO mapToDto(UnifiedDelivery entity) {
        if (entity == null) {
            return null;
        }
        return enrichQrFields(modelMapper.map(entity, UnifiedDeliveryDTO.class));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private UnifiedDeliveryDTO enrichQrFields(UnifiedDeliveryDTO dto) {
        if (dto == null) {
            return null;
        }

        if (isBlank(dto.getPublicCode()) && !isBlank(dto.getQrHex())) {
            dto.setPublicCode(dto.getQrHex());
        }
        if (isBlank(dto.getQrHex()) && !isBlank(dto.getPublicCode())) {
            dto.setQrHex(dto.getPublicCode());
        }

        if (isBlank(dto.getQrUrl()) && !isBlank(dto.getPublicCode())) {
            try {
                dto.setQrUrl(getQrUrlForPublicCode(dto.getPublicCode()));
            } catch (UnsupportedOperationException ignored) {
                // QR URL config is optional in some environments.
            }
        }

        if (dto.getQrImageBase64() != null && dto.getQrImageBase64().isBlank()) {
            dto.setQrImageBase64(null);
        }

        return dto;
    }

    @Override
    protected String getEntityType() {
        return "UNIFIEDDELIVERY";
    }

    @Override
    protected String getLabel(UnifiedDelivery entity) {
        if (entity == null) {
            return "Reception";
        }
        if (!isBlank(entity.getLotNumber())) {
            return entity.getLotNumber();
        }
        if (!isBlank(entity.getDeliveryNumber())) {
            return entity.getDeliveryNumber();
        }
        return "Reception " + entity.getId();
    }

    @Override
    protected String getStatus(UnifiedDelivery entity) {
        if (entity == null || entity.getStatus() == null) {
            return "UNKNOWN";
        }
        return entity.getStatus().name();
    }

    @Override
    protected String getMobileRoute() {
        return "/reception";
    }

    @Override
    protected String getWebRoute(UnifiedDelivery entity) {
        if (entity == null || entity.getId() == null) {
            return "/reception";
        }
        return "/reception/reception-details/" + entity.getId();
    }

    private void publishReceptionCreatedNotification(UnifiedDelivery delivery) {
        if (delivery == null || delivery.getId() == null) {
            return;
        }
        try {
            Map<String, String> fields = new HashMap<>();
            fields.put(
                    "deliveryType",
                    delivery.getDeliveryType() != null ? delivery.getDeliveryType().name() : "");
            notificationPort.publish(new NotificationEvent(
                    "UNIFIEDDELIVERY_CREATED",
                    delivery.getId(),
                    delivery.getLotNumber() != null ? delivery.getLotNumber() : delivery.getGlobalLotNumber(),
                    fields,
                    null,
                    null));
        } catch (Exception ex) {
            log.warn("Failed to publish reception created notification: {}", ex.getMessage());
        }
    }

}
