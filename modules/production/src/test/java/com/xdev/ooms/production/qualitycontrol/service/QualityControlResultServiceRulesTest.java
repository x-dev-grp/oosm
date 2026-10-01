package com.xdev.ooms.production.qualitycontrol.service;

import com.xdev.ooms.production.genealogy.repository.TraceabilityLotRepository;
import com.xdev.ooms.production.parameter.service.BooleanParameterReader;
import com.xdev.ooms.production.qualitycontrol.dto.QualityControlResultDto;
import com.xdev.ooms.production.qualitycontrol.entity.QualityControlResult;
import com.xdev.ooms.production.qualitycontrol.repository.QualityControlResultRepository;
import com.xdev.ooms.production.qualitycontrol.repository.QualityControlRuleRepository;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;
import com.xdev.ooms.production.unifieddelivery.repository.DeliveryRepository;
import com.xdev.ooms.production.unifieddelivery.service.UnifiedDeliveryService;
import com.xdev.ooms.sharedkernel.Enum.DeliveryType;
import com.xdev.ooms.sharedkernel.Enum.OliveLotStatus;
import com.xdev.ooms.sharedkernel.Enum.OperationType;
import com.xdev.ooms.sharedkernel.ports.NotificationPort;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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

    @ParameterizedTest
    @EnumSource(value = OliveLotStatus.class,
            names = {"COMPLETED", "IN_STOCK", "STOCK_READY", "WAITING_FOR_PRICING", "IN_PROGRESS", "CANCELLED"})
    void controlCannotBeRecordedOnceTheLotMovedOn(OliveLotStatus status) {
        UnifiedDelivery delivery = owned(OperationType.OLIVE_PURCHASE, status);

        assertThrows(IllegalArgumentException.class, () -> service.saveAll(List.of(result(delivery.getId()))));
        verify(resultRepository, never()).saveAll(anyList());
        verify(deliveryRepository, never()).save(any());
    }

    @Test
    void readyPurchaseCannotBeControlledAgain() {
        UnifiedDelivery delivery = owned(OperationType.OLIVE_PURCHASE, OliveLotStatus.PROD_READY);

        assertThrows(IllegalArgumentException.class, () -> service.saveAll(List.of(result(delivery.getId()))));
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
