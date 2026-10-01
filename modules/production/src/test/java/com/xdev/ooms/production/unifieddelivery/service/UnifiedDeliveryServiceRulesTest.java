package com.xdev.ooms.production.unifieddelivery.service;

import com.xdev.ooms.production.oiltransaction.service.OilTransactionService;
import com.xdev.ooms.production.parameter.service.ReceptionLimitsParameterReader;
import com.xdev.ooms.production.qualitycontrol.repository.QualityControlResultRepository;
import com.xdev.ooms.production.storageunit.repository.StorageUnitRepo;
import com.xdev.ooms.production.supplier.repository.SupplierRepository;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;
import com.xdev.ooms.production.unifieddelivery.repository.DeliveryRepository;
import com.xdev.ooms.sharedkernel.Enum.DeliveryType;
import com.xdev.ooms.sharedkernel.Enum.OliveLotStatus;
import com.xdev.ooms.sharedkernel.Enum.OperationType;
import com.xdev.ooms.sharedkernel.basetype.repository.GenericRepository;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.ports.FinancialTransactionPort;
import com.xdev.ooms.sharedkernel.ports.NotificationPort;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnifiedDeliveryServiceRulesTest {

    @Mock private BaseRepository<UnifiedDelivery> baseRepository;
    @Mock private DeliveryRepository deliveryRepository;
    @Mock private SupplierRepository supplierRepository;
    @Mock private StorageUnitRepo storageUnitRepo;
    @Mock private GenericRepository genericRepository;
    @Mock private OilTransactionService oilTransactionService;
    @Mock private FinancialTransactionPort financialTransactionPort;
    @Mock private QualityControlResultRepository qualityControlResultRepository;
    @Mock private NotificationPort notificationPort;
    @Mock private ReceptionLimitsParameterReader receptionLimitsParameterReader;
    @Mock private JdbcTemplate jdbcTemplate;

    private UnifiedDeliveryService service;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(UUID.randomUUID());
        service = new UnifiedDeliveryService(
                baseRepository, new ModelMapper(), deliveryRepository, supplierRepository, storageUnitRepo,
                genericRepository, oilTransactionService, financialTransactionPort,
                qualityControlResultRepository, notificationPort, receptionLimitsParameterReader, jdbcTemplate);
        lenient().when(deliveryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(baseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // --- cancellation ---------------------------------------------------------

    @Test
    void cancellationRecordsTheCause() {
        UnifiedDelivery delivery = owned(olive(OperationType.OLIVE_PURCHASE, OliveLotStatus.NEW));

        service.updateStatus(delivery.getId(), OliveLotStatus.CANCELLED, "Camion refuse");

        assertEquals(OliveLotStatus.CANCELLED, delivery.getStatus());
        assertTrue(delivery.getDescription().contains("Annulation : Camion refuse"));
    }

    @ParameterizedTest
    @EnumSource(value = OliveLotStatus.class, names = {"COMPLETED", "IN_STOCK", "STOCK_READY"})
    void anyStatusCanStillBeChanged(OliveLotStatus status) {
        UnifiedDelivery delivery = owned(olive(OperationType.SIMPLE_RECEPTION, status));

        service.updateStatus(delivery.getId(), OliveLotStatus.WAITING_FOR_PRICING, "Correction");

        assertEquals(OliveLotStatus.WAITING_FOR_PRICING, delivery.getStatus());
        assertTrue(delivery.getDescription().contains("Correction"));
    }

    @Test
    void receptionOfAnotherTenantIsNotFound() {
        UUID id = UUID.randomUUID();
        when(deliveryRepository.findOwned(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.updateStatus(id, OliveLotStatus.CANCELLED, null));
    }

    // --- pricing --------------------------------------------------------------

    @Test
    void olivePriceIsComputedOnNetWeightAndLotBecomesReady() {
        UnifiedDelivery delivery = owned(olive(OperationType.OLIVE_PURCHASE, OliveLotStatus.OLIVE_CONTROLLED));
        delivery.setPoidsNet(1000.0);

        service.updateprice(delivery.getId(), 1.5);

        assertEquals(1500.0, delivery.getPrice());
        assertEquals(1500.0, delivery.getUnpaidAmount());
        assertEquals(OliveLotStatus.PROD_READY, delivery.getStatus());
    }

    @Test
    void olivePriceCanStillBeSetBeforeQualityControl() {
        UnifiedDelivery delivery = owned(olive(OperationType.OLIVE_PURCHASE, OliveLotStatus.NEW));
        delivery.setPoidsNet(1000.0);

        service.updateprice(delivery.getId(), 1.5);

        assertEquals(1500.0, delivery.getPrice());
    }

    @Test
    void oilPriceMovesOilIntoStockOnce() {
        UnifiedDelivery delivery = owned(oil(OperationType.OIL_PURCHASE, OliveLotStatus.OIL_CONTROLLED));
        delivery.setOilQuantity(100.0);

        service.updateprice(delivery.getId(), 12.0);

        assertEquals(1200.0, delivery.getPrice());
        assertEquals(OliveLotStatus.IN_STOCK, delivery.getStatus());
        verify(oilTransactionService).createSingleOilTransactionIn(delivery);
    }

    @Test
    void oilAlreadyInStockCannotBeRepriced() {
        UnifiedDelivery delivery = owned(oil(OperationType.OIL_PURCHASE, OliveLotStatus.IN_STOCK));
        delivery.setOilQuantity(100.0);

        assertThrows(IllegalArgumentException.class, () -> service.updateprice(delivery.getId(), 12.0));
        verify(oilTransactionService, never()).createSingleOilTransactionIn(any());
    }

    @Test
    void priceMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> service.updateprice(UUID.randomUUID(), 0.0));
    }

    // --- deletion -------------------------------------------------------------

    @Test
    void newReceptionIsSoftDeletedAndItsFinanceReversed() {
        UnifiedDelivery delivery = owned(olive(OperationType.OLIVE_PURCHASE, OliveLotStatus.NEW));

        service.delete(delivery.getId());

        assertTrue(delivery.getDeleted());
        verify(financialTransactionPort).reverseLinked(any(), any());
    }

    @Test
    void paidMilledReceptionCanStillBeDeletedAndItsFinanceReversed() {
        UnifiedDelivery delivery = owned(olive(OperationType.SIMPLE_RECEPTION, OliveLotStatus.COMPLETED));
        delivery.setPaidAmount(10.0);

        service.delete(delivery.getId());

        assertTrue(delivery.getDeleted());
        verify(financialTransactionPort).reverseLinked(any(), any());
    }

    // --- helpers --------------------------------------------------------------

    private UnifiedDelivery owned(UnifiedDelivery delivery) {
        lenient().when(deliveryRepository.findOwned(delivery.getId())).thenReturn(Optional.of(delivery));
        return delivery;
    }

    private static UnifiedDelivery olive(OperationType operation, OliveLotStatus status) {
        return delivery(DeliveryType.OLIVE, operation, status);
    }

    private static UnifiedDelivery oil(OperationType operation, OliveLotStatus status) {
        return delivery(DeliveryType.OIL, operation, status);
    }

    private static UnifiedDelivery delivery(DeliveryType type, OperationType operation, OliveLotStatus status) {
        UnifiedDelivery delivery = new UnifiedDelivery();
        delivery.setId(UUID.randomUUID());
        delivery.setDeliveryType(type);
        delivery.setOperationType(operation);
        delivery.setStatus(status);
        delivery.setLotNumber("0001OC26");
        delivery.setPaidAmount(0.0);
        return delivery;
    }
}
