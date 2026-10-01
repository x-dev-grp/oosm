package com.xdev.ooms.production.qualitycontrol.service;

import com.xdev.ooms.production.genealogy.repository.TraceabilityLotRepository;
import com.xdev.ooms.production.parameter.service.BooleanParameterReader;
import com.xdev.ooms.production.qualitycontrol.dto.QualityControlResultDto;
import com.xdev.ooms.production.qualitycontrol.dto.QualityControlRuleDto;
import com.xdev.ooms.production.qualitycontrol.entity.QualityControlResult;
import com.xdev.ooms.production.qualitycontrol.entity.QualityControlRule;
import com.xdev.ooms.production.qualitycontrol.repository.QualityControlResultRepository;
import com.xdev.ooms.production.qualitycontrol.repository.QualityControlRuleRepository;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;
import com.xdev.ooms.production.unifieddelivery.repository.DeliveryRepository;
import com.xdev.ooms.production.unifieddelivery.service.UnifiedDeliveryService;
import com.xdev.ooms.sharedkernel.Enum.DeliveryType;
import com.xdev.ooms.sharedkernel.Enum.OliveLotStatus;
import com.xdev.ooms.sharedkernel.Enum.OperationType;
import com.xdev.ooms.sharedkernel.Enum.RuleType;
import com.xdev.ooms.sharedkernel.ports.NotificationPort;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QualityControlResultServiceRulesTest {

    @Mock private BaseRepository<QualityControlResult> baseRepository;
    @Mock private QualityControlResultRepository resultRepository;
    @Mock private QualityControlRuleRepository ruleRepository;
    @Mock private DeliveryRepository deliveryRepository;
    @Mock private UnifiedDeliveryService unifiedDeliveryService;
    @Mock private TraceabilityLotRepository traceabilityLotRepository;
    @Mock private NotificationPort notificationPort;
    @Mock private BooleanParameterReader booleanParameterReader;

    private QualityControlResultService service;

    @BeforeEach
    void setUp() {
        ModelMapper mapper = new ModelMapper();
        service = new QualityControlResultService(baseRepository, mapper, resultRepository, ruleRepository,
                deliveryRepository, mapper, unifiedDeliveryService, deliveryRepository, traceabilityLotRepository,
                notificationPort, booleanParameterReader);
    }

    @Test
    void emptyBatchIsANoOp() {
        assertTrue(service.saveAll(List.of()).isEmpty());
        verify(deliveryRepository, never()).findOwned(any());
    }

    @Test
    void resultsMustTargetASingleReception() {
        List<QualityControlResultDto> batch = List.of(result(UUID.randomUUID()), result(UUID.randomUUID()));

        assertThrows(IllegalArgumentException.class, () -> service.saveAll(batch));
    }

    @Test
    void receptionOfAnotherTenantIsRejected() {
        UUID id = UUID.randomUUID();
        when(deliveryRepository.findOwned(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.saveAll(List.of(result(id))));
    }

    @Test
    void stockedOilCannotBeControlledAgain() {
        UnifiedDelivery delivery = owned(OperationType.OIL_PURCHASE, OliveLotStatus.IN_STOCK);
        delivery.setDeliveryType(DeliveryType.OIL);

        assertThrows(IllegalArgumentException.class, () -> service.saveAll(List.of(result(delivery.getId()))));
        verify(resultRepository, never()).saveAll(anyList());
    }

    @Test
    void outOfRangeMeasureIsRejectedAndRuleWithoutTypeFlagIsAnOliveRule() {
        UnifiedDelivery delivery = owned(OperationType.OLIVE_PURCHASE, OliveLotStatus.COMPLETED);
        QualityControlRule rule = new QualityControlRule();
        rule.setId(UUID.randomUUID());
        rule.setRuleType(RuleType.NUMERIC);
        rule.setMinValue(0f);
        rule.setMaxValue(1f);
        when(ruleRepository.findAllById(any())).thenReturn(List.of(rule));
        QualityControlResultDto dto = result(delivery.getId());
        QualityControlRuleDto ruleDto = new QualityControlRuleDto();
        ruleDto.setId(rule.getId());
        dto.setRule(ruleDto);
        dto.setMeasuredValue("5");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> service.saveAll(List.of(dto)));
        assertTrue(error.getMessage().contains("maxValue"));
        verify(resultRepository, never()).saveAll(anyList());
    }

    private UnifiedDelivery owned(OperationType operation, OliveLotStatus status) {
        UnifiedDelivery delivery = new UnifiedDelivery();
        delivery.setId(UUID.randomUUID());
        delivery.setLotNumber("0001OC26");
        delivery.setDeliveryType(DeliveryType.OLIVE);
        delivery.setOperationType(operation);
        delivery.setStatus(status);
        when(deliveryRepository.findOwned(delivery.getId())).thenReturn(Optional.of(delivery));
        return delivery;
    }

    private static QualityControlResultDto result(UUID deliveryId) {
        QualityControlResultDto dto = new QualityControlResultDto();
        dto.setDeliveryId(deliveryId);
        return dto;
    }
}
