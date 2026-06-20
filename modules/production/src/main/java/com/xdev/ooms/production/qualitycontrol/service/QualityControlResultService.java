package com.xdev.ooms.production.qualitycontrol.service;


import com.xdev.ooms.production.genealogy.entity.TraceabilityLot;
import com.xdev.ooms.production.genealogy.repository.TraceabilityLotRepository;
import com.xdev.ooms.production.qualitycontrol.defaults.TunisiaOilGradeUtil;
import com.xdev.ooms.production.qualitycontrol.dto.QualityControlResultDto;
import com.xdev.ooms.production.qualitycontrol.repository.QualityControlResultRepository;
import com.xdev.ooms.production.qualitycontrol.repository.QualityControlRuleRepository;
import com.xdev.ooms.production.unifieddelivery.repository.DeliveryRepository;
import com.xdev.ooms.production.unifieddelivery.service.UnifiedDeliveryService;



import com.xdev.ooms.production.qualitycontrol.entity.QualityControlResult;
import com.xdev.ooms.production.qualitycontrol.entity.QualityControlRule;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;

import  com.xdev.ooms.sharedkernel.Enum.DeliveryType;
import  com.xdev.ooms.sharedkernel.Enum.OliveLotStatus;
import  com.xdev.ooms.sharedkernel.Enum.OperationType;
import  com.xdev.ooms.sharedkernel.Enum.RuleType;
import com.xdev.ooms.sharedkernel.models.Action;
import com.xdev.ooms.sharedkernel.ports.NotificationEvent;
import com.xdev.ooms.sharedkernel.ports.NotificationPort;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import com.xdev.ooms.sharedkernel.services.impl.BaseServiceImpl;
import com.xdev.ooms.sharedkernel.utils.OSMLogger;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QualityControlResultService extends BaseServiceImpl<QualityControlResult, QualityControlResultDto, QualityControlResultDto> {

    private static final Logger log = LoggerFactory.getLogger(QualityControlResultService.class);
    private final DeliveryRepository deliveryRepository;
    private final QualityControlResultRepository repository;
    private final QualityControlRuleRepository ruleRepository;
    private final DeliveryRepository deliveryRepo;
    private final TraceabilityLotRepository traceabilityLotRepository;
    private final ModelMapper modelMapper;
    private final UnifiedDeliveryService unifiedDeliveryService;
    private final NotificationPort notificationPort;
      private static final Set<String> allowedSet = Set.of(
              TunisiaOilGradeUtil.EXTRA_VIERGE,
              TunisiaOilGradeUtil.VIERGE,
              TunisiaOilGradeUtil.LAMPANTE,
              "Vierge Extra", "Extra", "EXTRA_VIRGIN", "VIRGIN", "LAMPANTE"
      );

    public QualityControlResultService(BaseRepository<QualityControlResult> repository, ModelMapper modelMapper, QualityControlResultRepository repository1, QualityControlRuleRepository ruleRepository, DeliveryRepository deliveryRepo, ModelMapper modelMapper1, UnifiedDeliveryService unifiedDeliveryService, DeliveryRepository deliveryRepository, TraceabilityLotRepository traceabilityLotRepository, NotificationPort notificationPort) {
        super(repository, modelMapper);
        this.repository = repository1;
        this.ruleRepository = ruleRepository;
        this.deliveryRepo = deliveryRepo;
        this.modelMapper = modelMapper1;
        this.unifiedDeliveryService = unifiedDeliveryService;
        this.deliveryRepository = deliveryRepository;
        this.traceabilityLotRepository = traceabilityLotRepository;
        this.notificationPort = notificationPort;
     }

    @Override
    public List<QualityControlResultDto> findAll() {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "findAll", null);
        try {
            throw new UnsupportedOperationException("Not implemented yet");
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "findAll", e);
            throw e;
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "findAll", null);
            OSMLogger.logPerformance(this.getClass(), "findAll", startTime, System.currentTimeMillis());
        }
    }

    @Override
    public Set<Action> actionsMapping(QualityControlResult result) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ));

        return actions;
    }

    @Transactional
    public List<QualityControlResultDto> saveAll(List<QualityControlResultDto> dtos) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "saveAll", dtos);
        if (dtos.isEmpty()) {
            OSMLogger.logMethodExit(this.getClass(), "saveAll", Collections.emptyList());
            OSMLogger.logPerformance(this.getClass(), "saveAll", startTime, System.currentTimeMillis());
            return Collections.emptyList();
        }

        // 1) Ensure all DTOs point to the same delivery
        UUID deliveryId = dtos.getFirst().getDeliveryId();
        if (dtos.stream().anyMatch(dto -> !deliveryId.equals(dto.getDeliveryId()))) {
            throw new IllegalArgumentException("All QualityControlResultDto must reference the same delivery");
        }

        // 2) Load that delivery
        UnifiedDelivery delivery = deliveryRepo.findById(deliveryId).orElseThrow(() -> new IllegalArgumentException("Delivery not found for ID " + deliveryId));

        // 3) Load & validate rules
        Map<UUID, QualityControlRule> ruleMap = fetchAndValidateRules(dtos);


        // 5) Map each DTO → entity
        List<QualityControlResult> entities = dtos.stream().map(dto -> {
            QualityControlRule rule = ruleMap.get(dto.getRule().getId());
            validateMeasuredValue(dto.getMeasuredValue(), rule);
            QualityControlResult e = new QualityControlResult();
            e.setRule(rule);
            e.setMeasuredValue(dto.getMeasuredValue());
            e.setDelivery(delivery);
            return e;
        }).toList();

        Optional<QualityControlResult> match = entities.stream()
                .filter(qcr -> allowedSet.contains(qcr.getMeasuredValue()))
                .findFirst();

        if (match.isPresent()) {
            delivery.setCategoryOliveOil(TunisiaOilGradeUtil.normalizeCategory(match.get().getMeasuredValue()));
        } else {
            suggestCategoryFromMeasurements(entities).ifPresent(delivery::setCategoryOliveOil);
        }

        // 6) Persist QC results
        List<QualityControlResult> saved = repository.saveAll(entities);

        // 7) Mark delivery as quality-checked
        delivery.setHasQualityControl(true);

        if (delivery.getOperationType() == OperationType.BASE && delivery.getDeliveryType() == DeliveryType.OLIVE) {
            delivery.setStatus(OliveLotStatus.PROD_READY);
        } else {
            delivery.setStatus(delivery.getDeliveryType() == DeliveryType.OIL ? OliveLotStatus.OIL_CONTROLLED : OliveLotStatus.OLIVE_CONTROLLED);
        }
        deliveryRepo.save(delivery);
        publishQcCompletedNotification(delivery);

        // 8) Map back to DTOs
        List<QualityControlResultDto> resultDtos = saved.stream().map(e -> modelMapper.map(e, QualityControlResultDto.class)).toList();
        OSMLogger.logMethodExit(this.getClass(), "saveAll", resultDtos);
        OSMLogger.logPerformance(this.getClass(), "saveAll", startTime, System.currentTimeMillis());
        return resultDtos;
    }

    @Transactional
    public List<QualityControlResultDto> saveOilQcForOliveRec(UUID idx, List<QualityControlResultDto> dtos, String std) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "saveAllForIdx", idx, dtos);
        if (dtos.isEmpty()) {
            OSMLogger.logMethodExit(this.getClass(), "saveAllForIdx", Collections.emptyList());
            OSMLogger.logPerformance(this.getClass(), "saveAllForIdx", startTime, System.currentTimeMillis());
            return Collections.emptyList();
        }
        UnifiedDelivery newOIlRec = unifiedDeliveryService.createOilRecFromOliveRecImpl(idx, true, std);
        log.info("Saving QC results for idx: {} ({} results)", idx, dtos.size());
        // Validate rules
        Map<UUID, QualityControlRule> ruleMap = fetchAndValidateRules(dtos);
        // Map each DTO → entity (no delivery linkage)
        List<QualityControlResult> entities = dtos.stream().map(dto -> {
            QualityControlRule rule = ruleMap.get(dto.getRule().getId());
            validateMeasuredValue(dto.getMeasuredValue(), rule);
            QualityControlResult e = new QualityControlResult();
            e.setRule(rule);
            e.setMeasuredValue(dto.getMeasuredValue());
            e.setDelivery(newOIlRec);
            return e;
        }).toList();

        Optional<QualityControlResult> match = entities.stream()
                .filter(qcr -> allowedSet.contains(qcr.getMeasuredValue()))
                .findFirst();
        if (match.isPresent()) {
            newOIlRec.setCategoryOliveOil(TunisiaOilGradeUtil.normalizeCategory(match.get().getMeasuredValue()));
        } else {
            suggestCategoryFromMeasurements(entities).ifPresent(newOIlRec::setCategoryOliveOil);
        }

        // Persist QC results
        List<QualityControlResult> saved = repository.saveAll(entities);

        newOIlRec.setHasQualityControl(true);
        newOIlRec.setStatus(OliveLotStatus.OIL_CONTROLLED);
        deliveryRepo.save(newOIlRec);
        publishQcCompletedNotification(newOIlRec);

        // Map back to DTOs
        List<QualityControlResultDto> resultDtos = saved.stream().map(e -> modelMapper.map(e, QualityControlResultDto.class)).toList();
        OSMLogger.logMethodExit(this.getClass(), "saveAllForIdx", resultDtos);
        OSMLogger.logPerformance(this.getClass(), "saveAllForIdx", startTime, System.currentTimeMillis());
        return resultDtos;
    }

    @Transactional
    public List<QualityControlResultDto> saveForFiltration(UUID filtrationOperationId, List<QualityControlResultDto> dtos) {
        if (filtrationOperationId == null) {
            throw new IllegalArgumentException("Filtration operation ID is required");
        }
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }

        UUID traceabilityLotId = traceabilityLotRepository
                .findFirstByFiltrationOperationIdAndIsDeletedFalseOrderByCapturedAtDesc(filtrationOperationId)
                .map(com.xdev.ooms.production.genealogy.entity.TraceabilityLot::getId)
                .orElse(null);

        Map<UUID, QualityControlRule> ruleMap = fetchAndValidateRules(dtos);

        List<QualityControlResult> entities = dtos.stream().map(dto -> {
            QualityControlRule rule = ruleMap.get(dto.getRule().getId());
            validateMeasuredValue(dto.getMeasuredValue(), rule);

            QualityControlResult entity = new QualityControlResult();
            entity.setRule(rule);
            entity.setMeasuredValue(dto.getMeasuredValue());
            entity.setFiltrationOperationId(filtrationOperationId);
            entity.setTraceabilityLotId(dto.getTraceabilityLotId() != null ? dto.getTraceabilityLotId() : traceabilityLotId);
            return entity;
        }).toList();

        return repository.saveAll(entities).stream()
                .map(this::toDto)
                .toList();
    }

    // ——————————————————————————————————

    // Helper: fetch & validate rule IDs
    private Map<UUID, QualityControlRule> fetchAndValidateRules(List<QualityControlResultDto> dtos) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "fetchAndValidateRules", dtos);
        Set<UUID> ruleIds = dtos.stream().peek(dto -> {
            if (dto.getRule() == null || dto.getRule().getId() == null) {
                throw new IllegalArgumentException("Each DTO must reference a valid Rule ID");
            }
        }).map(dto -> dto.getRule().getId()).collect(Collectors.toSet());

        List<QualityControlRule> rules = ruleRepository.findAllById(ruleIds);
        if (rules.size() != ruleIds.size()) {
            throw new IllegalArgumentException("One or more provided Rule IDs were not found");
        }
        Map<UUID, QualityControlRule> ruleMap = rules.stream().collect(Collectors.toMap(QualityControlRule::getId, Function.identity()));
        OSMLogger.logMethodExit(this.getClass(), "fetchAndValidateRules", ruleMap);
        OSMLogger.logPerformance(this.getClass(), "fetchAndValidateRules", startTime, System.currentTimeMillis());
        return ruleMap;
    }

    // ✅ extracted validation
    private static final double NUMERIC_EPSILON = 1e-6;

    private void validateMeasuredValue(String measuredValue, QualityControlRule rule) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "validateMeasuredValue", measuredValue, rule);
        RuleType ruleType = rule.getRuleType();

        switch (ruleType) {
            case NUMERIC:
                try {
                    Double value = Double.parseDouble(measuredValue);
                    if (rule.getMinValue() != null && value < rule.getMinValue() - NUMERIC_EPSILON) {
                        throw new IllegalArgumentException("Measured value below minValue for rule ID: " + rule.getId());
                    }
                    if (rule.getMaxValue() != null && value > rule.getMaxValue() + NUMERIC_EPSILON) {
                        throw new IllegalArgumentException("Measured value above maxValue for rule ID: " + rule.getId());
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid numeric value for rule ID: " + rule.getId());
                }
                break;

            case BOOLEAN:
                if (!"true".equalsIgnoreCase(measuredValue) && !"false".equalsIgnoreCase(measuredValue)) {
                    throw new IllegalArgumentException("Invalid boolean value for rule ID: " + rule.getId());
                }
                break;

            case STRING:
                String allowedText = rule.getRuleTextValue();
                if (allowedText != null && !allowedText.trim().isEmpty()) {
                    List<String> allowedValues = Arrays.stream(allowedText.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
                    if (!allowedValues.contains(measuredValue)) {
                        throw new IllegalArgumentException("Invalid string value for rule ID: " + rule.getId() + ". Allowed values are: " + allowedValues);
                    }
                }
                break;


            default:
                throw new IllegalArgumentException("Unknown rule type: " + ruleType);
        }
        OSMLogger.logMethodExit(this.getClass(), "validateMeasuredValue", null);
        OSMLogger.logPerformance(this.getClass(), "validateMeasuredValue", startTime, System.currentTimeMillis());
    }


    @Transactional(readOnly = true)
    public List<QualityControlResultDto> findByDeliveryId(UUID deliveryId) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "findByDeliveryId", deliveryId);
        log.debug("Fetching quality control results for deliveryId: {}", deliveryId);
        if (deliveryId == null) {
            log.error("Delivery ID is null");
            throw new IllegalArgumentException("Delivery ID is required");
        }

        List<QualityControlResult> results = repository.findByDeliveryId(deliveryId);
        log.debug("Found {} quality control results for deliveryId: {}", results.size(), deliveryId);

        List<QualityControlResultDto> resultDtos = results.stream().map(entity -> modelMapper.map(entity, QualityControlResultDto.class)).collect(Collectors.toList());
        OSMLogger.logMethodExit(this.getClass(), "findByDeliveryId", resultDtos);
        OSMLogger.logPerformance(this.getClass(), "findByDeliveryId", startTime, System.currentTimeMillis());
        return resultDtos;
    }

    @Transactional(readOnly = true)
    public List<QualityControlResultDto> findByFiltrationOperationId(UUID filtrationOperationId) {
        if (filtrationOperationId == null) {
            throw new IllegalArgumentException("Filtration operation ID is required");
        }

        return repository.findByFiltrationOperationIdAndIsDeletedFalse(filtrationOperationId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QualityControlResultDto> findByTraceabilityLotId(UUID traceabilityLotId) {
        if (traceabilityLotId == null) {
            throw new IllegalArgumentException("Traceability lot ID is required");
        }

        return repository.findByTraceabilityLotIdAndIsDeletedFalse(traceabilityLotId).stream()
                .map(this::toDto)
                .toList();
    }

    private QualityControlResultDto toDto(QualityControlResult entity) {
        QualityControlResultDto dto = modelMapper.map(entity, QualityControlResultDto.class);
        dto.setFiltrationOperationId(entity.getFiltrationOperationId());
        dto.setTraceabilityLotId(entity.getTraceabilityLotId());
        if (entity.getDelivery() != null) {
            dto.setDeliveryId(entity.getDelivery().getId());
        }
        return dto;
    }

    private Optional<String> suggestCategoryFromMeasurements(List<QualityControlResult> entities) {
        Double acidity = null;
        Double k232 = null;
        Double k270 = null;
        Double deltaK = null;
        Double peroxide = null;

        for (QualityControlResult result : entities) {
            if (result.getRule() == null || result.getRule().getRuleType() != RuleType.NUMERIC) {
                continue;
            }
            try {
                double value = Double.parseDouble(result.getMeasuredValue());
                String key = result.getRule().getRuleKey();
                if (key == null) {
                    continue;
                }
                switch (key) {
                    case "Acidite" -> acidity = value;
                    case "K232" -> k232 = value;
                    case "K270" -> k270 = value;
                    case "DeltaK" -> deltaK = value;
                    case "IndicePreoxyde" -> peroxide = value;
                    default -> { }
                }
            } catch (NumberFormatException ignored) {
                // skip invalid numeric values
            }
        }

        return TunisiaOilGradeUtil.suggestOilGrade(acidity, k232, k270, deltaK, peroxide);
    }

    private void publishQcCompletedNotification(UnifiedDelivery delivery) {
        if (delivery == null || delivery.getId() == null) {
            return;
        }
        try {
            Map<String, String> fields = new HashMap<>();
            fields.put(
                    "deliveryType",
                    delivery.getDeliveryType() != null ? delivery.getDeliveryType().name() : "");
            fields.put("status", delivery.getStatus() != null ? delivery.getStatus().name() : "");
            notificationPort.publish(new NotificationEvent(
                    "UNIFIEDDELIVERY_QC_COMPLETED",
                    delivery.getId(),
                    delivery.getLotNumber() != null ? delivery.getLotNumber() : delivery.getGlobalLotNumber(),
                    fields,
                    null,
                    null));
        } catch (Exception ex) {
            log.warn("Failed to publish QC completed notification: {}", ex.getMessage());
        }
    }

//    @Transactional(readOnly = true)
//    public List<QualityControlResultDto> findOilResultsByOliveDeliveryFromOliveLotNumber(String oliveLotNUmber) {
//        long startTime = System.currentTimeMillis();
//        OSMLogger.logMethodEntry(this.getClass(), "findOilResultsByOliveDeliveryFromOliveLotNumber", oliveLotNUmber);
////UUID oilRecFromOliveRec_Lotnumber = deliveryRepository.findByLotOliveNumber(oliveLotNUmber).getFirst().getQualityControlResults()
////        List<QualityControlResult> results = repository.findByDeliveryIdAndRule_OilQcTrue(deliveryId);
//         List<QualityControlResult> results = (List<QualityControlResult>) deliveryRepository.findByLotOliveNumber(oliveLotNUmber).getFirst().getQualityControlResults();
//        List<QualityControlResultDto> resultDtos = results.stream()
//                .map(e -> modelMapper.map(e, QualityControlResultDto.class))
//                .toList();
//        OSMLogger.logMethodExit(this.getClass(), "findOilResultsByOliveDeliveryFromOliveLotNumber", resultDtos);
//        OSMLogger.logPerformance(this.getClass(), "findOilResultsByOliveDeliveryFromOliveLotNumber", startTime, System.currentTimeMillis());
//        return resultDtos;
//    }
}
