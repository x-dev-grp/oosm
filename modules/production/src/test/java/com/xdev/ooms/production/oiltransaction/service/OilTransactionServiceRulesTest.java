package com.xdev.ooms.production.oiltransaction.service;

import com.xdev.ooms.production.oiltransaction.dto.OilTransactionDTO;
import com.xdev.ooms.production.oiltransaction.entity.OilTransaction;
import com.xdev.ooms.production.oiltransaction.repository.OilTransactionRepository;
import com.xdev.ooms.production.storageunit.dto.StorageUnitDto;
import com.xdev.ooms.production.storageunit.entity.StorageUnit;
import com.xdev.ooms.production.storageunit.repository.StorageUnitRepo;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;
import com.xdev.ooms.production.unifieddelivery.repository.DeliveryRepository;
import com.xdev.ooms.sharedkernel.Enum.DeliveryType;
import com.xdev.ooms.sharedkernel.Enum.TransactionState;
import com.xdev.ooms.sharedkernel.Enum.TransactionType;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.ports.OilCreditPort;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

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
class OilTransactionServiceRulesTest {

    @Mock private OilTransactionRepository repository;
    @Mock private StorageUnitRepo storageUnitRepo;
    @Mock private OilCreditPort oilCreditPort;
    @Mock private DeliveryRepository deliveryRepository;

    private final UUID tenantId = UUID.randomUUID();
    private OilTransactionService service;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(tenantId);
        service = new OilTransactionService(repository, new ModelMapper(), storageUnitRepo, oilCreditPort,
                deliveryRepository, deliveryRepository);
        lenient().when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void pendingTransactionDoesNotMoveStock() {
        StorageUnit tank = tank(tenantId, 200.0);
        when(storageUnitRepo.findByIdAndIsDeletedFalse(tank.getId())).thenReturn(Optional.of(tank));

        service.save(request(TransactionState.PENDING, tank, 100.0));

        assertEquals(200.0, tank.getCurrentVolume());
        verify(storageUnitRepo, never()).save(any());
    }

    @Test
    void completedTransactionMovesStockIntoDestination() {
        StorageUnit tank = tank(tenantId, 200.0);
        when(storageUnitRepo.findByIdAndIsDeletedFalse(tank.getId())).thenReturn(Optional.of(tank));

        service.save(request(TransactionState.COMPLETED, tank, 100.0));

        assertEquals(300.0, tank.getCurrentVolume());
        verify(storageUnitRepo).save(tank);
    }

    @Test
    void saveRefusesTankOfAnotherTenant() {
        StorageUnit foreign = tank(UUID.randomUUID(), 200.0);
        when(storageUnitRepo.findByIdAndIsDeletedFalse(foreign.getId())).thenReturn(Optional.of(foreign));

        assertThrows(EntityNotFoundException.class,
                () -> service.save(request(TransactionState.COMPLETED, foreign, 100.0)));
        assertEquals(200.0, foreign.getCurrentVolume());
    }

    @Test
    void approvalRefusesTransactionThatIsNotPending() {
        OilTransaction tx = transaction(TransactionState.COMPLETED);
        when(repository.findByIdAndIsDeletedFalse(tx.getId())).thenReturn(Optional.of(tx));
        OilTransactionDTO dto = new OilTransactionDTO();
        dto.setId(tx.getId());

        assertThrows(IllegalStateException.class, () -> service.approveOilTransaction2(dto));
    }

    @Test
    void deletingPendingTransactionLeavesStockUntouched() {
        StorageUnit tank = tank(tenantId, 200.0);
        OilTransaction tx = transaction(TransactionState.PENDING);
        tx.setStorageUnitDestination(tank);
        when(repository.findByIdAndIsDeletedFalse(tx.getId())).thenReturn(Optional.of(tx));

        service.delete(tx.getId());

        assertTrue(tx.getDeleted());
        assertEquals(200.0, tank.getCurrentVolume());
        verify(storageUnitRepo, never()).save(any());
    }

    @Test
    void deletingCompletedSaleRestoresSourceTankAtAverageCost() {
        StorageUnit tank = tank(tenantId, 200.0);
        OilTransaction tx = transaction(TransactionState.COMPLETED);
        tx.setTransactionType(TransactionType.OIL_SALE);
        tx.setStorageUnitSource(tank);
        tx.setUnitPrice(15.0);
        when(repository.findByIdAndIsDeletedFalse(tx.getId())).thenReturn(Optional.of(tx));

        service.delete(tx.getId());

        assertEquals(300.0, tank.getCurrentVolume());
        assertEquals(1200.0, tank.getTotalCost());
    }

    @Test
    void deletingTransactionLinkedToSaleIsRefused() {
        OilTransaction tx = transaction(TransactionState.PENDING);
        tx.setOilSaleId(UUID.randomUUID());
        when(repository.findByIdAndIsDeletedFalse(tx.getId())).thenReturn(Optional.of(tx));

        assertThrows(IllegalStateException.class, () -> service.delete(tx.getId()));
        verify(repository, never()).save(any());
    }

    @Test
    void updatingValidatedTransactionIsRefused() {
        OilTransaction tx = transaction(TransactionState.COMPLETED);
        when(repository.findByIdAndIsDeletedFalse(tx.getId())).thenReturn(Optional.of(tx));
        OilTransactionDTO dto = new OilTransactionDTO();
        dto.setId(tx.getId());
        dto.setQuantityKg(999.0);

        assertThrows(IllegalStateException.class, () -> service.update(dto));
        assertEquals(100.0, tx.getQuantityKg());
    }

    @Test
    void updatingPendingTransactionKeepsItPending() {
        OilTransaction tx = transaction(TransactionState.PENDING);
        when(repository.findByIdAndIsDeletedFalse(tx.getId())).thenReturn(Optional.of(tx));
        OilTransactionDTO dto = new OilTransactionDTO();
        dto.setId(tx.getId());
        dto.setQuantityKg(150.0);
        dto.setTransactionState(TransactionState.COMPLETED);

        service.update(dto);

        assertEquals(150.0, tx.getQuantityKg());
        assertEquals(TransactionState.PENDING, tx.getTransactionState());
    }

    @Test
    void receptionOilCannotEnterStockTwice() {
        UnifiedDelivery delivery = new UnifiedDelivery();
        delivery.setId(UUID.randomUUID());
        delivery.setDeliveryType(DeliveryType.OIL);
        delivery.setOilQuantity(100.0);
        delivery.setUnitPrice(12.0);
        when(repository.findFirstByReceptionIdAndTransactionTypeAndIsDeletedFalse(delivery.getId(), TransactionType.RECEPTION_IN))
                .thenReturn(Optional.of(transaction(TransactionState.COMPLETED)));

        assertThrows(IllegalArgumentException.class, () -> service.createSingleOilTransactionIn(delivery));
        verify(repository, never()).save(any());
        verify(storageUnitRepo, never()).save(any());
    }

    private OilTransactionDTO request(TransactionState state, StorageUnit destination, double quantity) {
        OilTransactionDTO dto = new OilTransactionDTO();
        dto.setTransactionType(TransactionType.RECEPTION_IN);
        dto.setTransactionState(state);
        dto.setQuantityKg(quantity);
        dto.setUnitPrice(5.0);
        StorageUnitDto ref = new StorageUnitDto();
        ref.setId(destination.getId());
        dto.setStorageUnitDestination(ref);
        return dto;
    }

    private OilTransaction transaction(TransactionState state) {
        OilTransaction tx = new OilTransaction();
        tx.setId(UUID.randomUUID());
        tx.setTenantId(tenantId);
        tx.setTransactionType(TransactionType.RECEPTION_IN);
        tx.setTransactionState(state);
        tx.setQuantityKg(100.0);
        tx.setUnitPrice(4.0);
        return tx;
    }

    private StorageUnit tank(UUID owner, double volume) {
        StorageUnit tank = new StorageUnit();
        tank.setId(UUID.randomUUID());
        tank.setTenantId(owner);
        tank.setName("Cuve");
        tank.setMaxCapacity(1000.0);
        tank.setCurrentVolume(volume);
        tank.setAvgCost(4.0);
        tank.setTotalCost(volume * 4.0);
        return tank;
    }
}
