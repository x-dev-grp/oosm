package com.xdev.ooms.production.planning.service;

import com.xdev.ooms.production.maintenance.service.MillMachineAvailabilityService;
import com.xdev.ooms.production.millmachine.entity.MillMachine;
import com.xdev.ooms.production.millmachine.repository.MillMachineRepository;
import com.xdev.ooms.production.oiltransaction.service.OilTransactionService;
import com.xdev.ooms.production.parameter.service.ReceptionLimitsParameterReader;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;
import com.xdev.ooms.production.unifieddelivery.repository.DeliveryRepository;
import com.xdev.ooms.production.unifieddelivery.service.UnifiedDeliveryService;
import com.xdev.ooms.sharedkernel.Enum.DeliveryType;
import com.xdev.ooms.sharedkernel.Enum.OliveLotStatus;
import com.xdev.ooms.sharedkernel.Enum.OperationType;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.ports.NotificationPort;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.AfterEach;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanningServiceCompletionTest {

    private static final String LOT = "0001OC26";

    @Mock private MillMachineRepository millRepo;
    @Mock private DeliveryRepository deliveryRepo;
    @Mock private UnifiedDeliveryService unifiedDeliveryService;
    @Mock private OilTransactionService oilTransactionService;
    @Mock private NotificationPort notificationPort;
    @Mock private MillMachineAvailabilityService millMachineAvailabilityService;
    @Mock private ReceptionLimitsParameterReader receptionLimitsParameterReader;

    private final UUID tenantId = UUID.randomUUID();
    private PlanningService service;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(tenantId);
        service = new PlanningService(millRepo, deliveryRepo, new ModelMapper(), unifiedDeliveryService,
                oilTransactionService, notificationPort, millMachineAvailabilityService, receptionLimitsParameterReader);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void unknownLotIsNotFound() {
        when(deliveryRepo.findAllByLotNumberAndDeliveryTypeAndIsDeletedFalse(LOT, DeliveryType.OLIVE)).thenReturn(List.of());

        assertThrows(EntityNotFoundException.class, () -> complete(500.0, 100.0, null));
    }

    @ParameterizedTest
    @EnumSource(value = OliveLotStatus.class, names = {"NEW", "COMPLETED", "CANCELLED", "WAITING_FOR_PRICING"})
    void lotOutsideMillingFlowCannotBeCompleted(OliveLotStatus status) {
        stubLot(lot(OperationType.SIMPLE_RECEPTION, status));

        assertThrows(ValidationException.class, () -> complete(500.0, 100.0, null));
        verify(deliveryRepo, never()).save(any());
    }

    @Test
    void oilQuantityIsRequired() {
        stubLot(lot(OperationType.SIMPLE_RECEPTION, OliveLotStatus.IN_PROGRESS));

        assertThrows(ValidationException.class, () -> complete(null, 100.0, null));
    }

    @Test
    void simpleReceptionNeedsServicePrice() {
        stubLot(lot(OperationType.SIMPLE_RECEPTION, OliveLotStatus.IN_PROGRESS));

        assertThrows(ValidationException.class, () -> complete(500.0, null, null));
    }

    @Test
    void millIsRequired() {
        stubLot(lot(OperationType.SIMPLE_RECEPTION, OliveLotStatus.IN_PROGRESS));

        assertThrows(ValidationException.class, () -> complete(500.0, 100.0, null));
        verify(deliveryRepo, never()).save(any());
    }

    @Test
    void simpleReceptionIsCompletedWithServicePriceAndMillHours() {
        UnifiedDelivery lot = stubLot(lot(OperationType.SIMPLE_RECEPTION, OliveLotStatus.IN_PROGRESS));
        lot.setPoidsNet(2000.0);
        MillMachine mill = mill(10L);
        lot.setMillMachine(mill);

        service.markLotCompleted(LOT, null, 400.0, 20.0, 300.0, false, 90, null, null, null);

        assertEquals(OliveLotStatus.COMPLETED, lot.getStatus());
        assertEquals(400.0, lot.getOilQuantity());
        assertEquals(300.0, lot.getPrice());
        assertEquals(300.0, lot.getUnpaidAmount());
        assertEquals(0.15, lot.getUnitPrice(), 1e-9);
        assertEquals(12L, mill.getHoursOperated());
        verify(unifiedDeliveryService, never()).createOilRecFromOliveRecImpl(any(), any(Boolean.class), any());
    }

    @Test
    void purchaseKeepsItsPurchasePriceAndCreatesOilReception() {
        UnifiedDelivery lot = stubLot(lot(OperationType.OLIVE_PURCHASE, OliveLotStatus.PROD_READY));
        lot.setUnitPrice(1.5);
        lot.setPrice(3000.0);
        lot.setMillMachine(mill(0L));

        service.markLotCompleted(LOT, null, 400.0, 20.0, null, false, 60, null, null, null);

        assertEquals(1.5, lot.getUnitPrice());
        assertEquals(3000.0, lot.getPrice());
        verify(unifiedDeliveryService).createOilRecFromOliveRecImpl(lot.getId(), false, null);
    }

    private void complete(Double oilQuantity, Double servicePrice, UUID millId) {
        service.markLotCompleted(LOT, null, oilQuantity, 20.0, servicePrice, false, 60, null, null, millId);
    }

    private UnifiedDelivery stubLot(UnifiedDelivery lot) {
        when(deliveryRepo.findAllByLotNumberAndDeliveryTypeAndIsDeletedFalse(LOT, DeliveryType.OLIVE)).thenReturn(List.of(lot));
        return lot;
    }

    private MillMachine mill(long hours) {
        MillMachine mill = new MillMachine();
        mill.setId(UUID.randomUUID());
        mill.setHoursOperated(hours);
        when(millRepo.findByIdAndTenantIdAndIsDeletedFalse(mill.getId(), tenantId)).thenReturn(Optional.of(mill));
        return mill;
    }

    private static UnifiedDelivery lot(OperationType operation, OliveLotStatus status) {
        UnifiedDelivery lot = new UnifiedDelivery();
        lot.setId(UUID.randomUUID());
        lot.setLotNumber(LOT);
        lot.setDeliveryType(DeliveryType.OLIVE);
        lot.setOperationType(operation);
        lot.setStatus(status);
        return lot;
    }
}
