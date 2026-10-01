package com.xdev.ooms.production.unifieddelivery.service;

import com.xdev.ooms.production.oiltransaction.service.OilTransactionService;
import com.xdev.ooms.production.parameter.service.ReceptionLimitsParameterReader;
import com.xdev.ooms.production.qualitycontrol.repository.QualityControlResultRepository;
import com.xdev.ooms.production.storageunit.repository.StorageUnitRepo;
import com.xdev.ooms.production.supplier.repository.SupplierRepository;
import com.xdev.ooms.production.unifieddelivery.dto.PaymentDTO;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;
import com.xdev.ooms.production.unifieddelivery.repository.DeliveryRepository;
import com.xdev.ooms.sharedkernel.Enum.Currency;
import com.xdev.ooms.sharedkernel.Enum.OperationType;
import com.xdev.ooms.sharedkernel.Enum.PaymentMethod;
import com.xdev.ooms.sharedkernel.Enum.TransactionDirection;
import com.xdev.ooms.sharedkernel.Enum.TransactionType;
import com.xdev.ooms.sharedkernel.basetype.repository.GenericRepository;
import com.xdev.ooms.sharedkernel.communicator.models.shared.FinancialTransactionDto;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.ports.FinancialTransactionPort;
import com.xdev.ooms.sharedkernel.ports.NotificationPort;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnifiedDeliveryServicePaymentTest {

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

    private final ModelMapper modelMapper = new ModelMapper();
    private final UUID tenantId = UUID.randomUUID();
    private UnifiedDeliveryService service;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(tenantId);
        service = new UnifiedDeliveryService(
                baseRepository, modelMapper, deliveryRepository, supplierRepository, storageUnitRepo,
                genericRepository, oilTransactionService, financialTransactionPort,
                qualityControlResultRepository, notificationPort, receptionLimitsParameterReader);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void partialPaymentPersistsPaidAndUnpaidAmountsAndKeepsPaidFalse() {
        UnifiedDelivery delivery = delivery(OperationType.OLIVE_PURCHASE, 100, 0);
        stubOwnedDelivery(delivery);

        service.processPayment(payment(delivery.getId(), 40, PaymentMethod.CASH));

        assertEquals(40, delivery.getPaidAmount());
        assertEquals(60, delivery.getUnpaidAmount());
        assertFalse(delivery.getPaid());
        FinancialTransactionDto transaction = capturedTransaction();
        assertEquals(BigDecimal.valueOf(40.0), transaction.getAmount());
        assertEquals(TransactionDirection.OUTBOUND, transaction.getDirection());
        assertEquals(TransactionType.PURCHASE, transaction.getTransactionType());
        assertEquals(PaymentMethod.CASH, transaction.getPaymentMethod());
    }

    @Test
    void finalPaymentClearsBalanceAndMarksDeliveryPaid() {
        UnifiedDelivery delivery = delivery(OperationType.OLIVE_PURCHASE, 100, 40);
        stubOwnedDelivery(delivery);

        service.processPayment(payment(delivery.getId(), 60, PaymentMethod.TRANSFER));

        assertEquals(100, delivery.getPaidAmount());
        assertEquals(0, delivery.getUnpaidAmount());
        assertTrue(delivery.getPaid());
    }

    @Test
    void overpaymentIsCappedAtRemainingBalance() {
        UnifiedDelivery delivery = delivery(OperationType.OLIVE_PURCHASE, 100, 70);
        stubOwnedDelivery(delivery);

        service.processPayment(payment(delivery.getId(), 100, PaymentMethod.CHEQUE));

        assertEquals(100, delivery.getPaidAmount());
        assertEquals(0, delivery.getUnpaidAmount());
        assertEquals(BigDecimal.valueOf(30.0), capturedTransaction().getAmount());
    }

    @Test
    void exchangeOilPaymentCreatesInboundPaymentTransaction() {
        UnifiedDelivery delivery = delivery(OperationType.EXCHANGE, 120, 0);
        stubOwnedDelivery(delivery);

        service.processPayment(payment(delivery.getId(), 120, PaymentMethod.OIL));

        assertTrue(delivery.getPaid());
        FinancialTransactionDto transaction = capturedTransaction();
        assertEquals(TransactionDirection.INBOUND, transaction.getDirection());
        assertEquals(TransactionType.PAYMENT, transaction.getTransactionType());
        assertEquals(OperationType.EXCHANGE, transaction.getOperationType());
        assertEquals(PaymentMethod.OIL, transaction.getPaymentMethod());
    }

    @Test
    void paymentLookupIsScopedToCurrentTenant() {
        UUID deliveryId = UUID.randomUUID();
        when(deliveryRepository.findByIdAndTenantIdAndIsDeletedFalse(deliveryId, tenantId))
                .thenReturn(Optional.empty());

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.processPayment(payment(deliveryId, 25, PaymentMethod.CASH)));

        assertTrue(error.getMessage().contains("Delivery not found"));
        verify(deliveryRepository).findByIdAndTenantIdAndIsDeletedFalse(deliveryId, tenantId);
        verifyNoInteractions(financialTransactionPort);
    }

    @Test
    void zeroPaymentIsRejectedBeforeDatabaseMutation() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.processPayment(payment(UUID.randomUUID(), 0, PaymentMethod.CASH)));

        assertEquals("Payment amount must be greater than 0", error.getMessage());
        verifyNoInteractions(deliveryRepository, financialTransactionPort);
    }

    private UnifiedDelivery delivery(OperationType operationType, double price, double paidAmount) {
        UnifiedDelivery delivery = new UnifiedDelivery();
        delivery.setId(UUID.randomUUID());
        delivery.setTenantId(tenantId);
        delivery.setLotNumber("LOT-1");
        delivery.setOperationType(operationType);
        delivery.setPrice(price);
        delivery.setPaidAmount(paidAmount);
        delivery.setUnpaidAmount(price - paidAmount);
        delivery.setPaid(paidAmount >= price);
        return delivery;
    }

    private PaymentDTO payment(UUID deliveryId, double amount, PaymentMethod method) {
        PaymentDTO payment = new PaymentDTO();
        payment.setIdOperation(deliveryId);
        payment.setAmount(amount);
        payment.setCurrency(Currency.TND);
        payment.setPaymentMethod(method);
        return payment;
    }

    private void stubOwnedDelivery(UnifiedDelivery delivery) {
        when(deliveryRepository.findByIdAndTenantIdAndIsDeletedFalse(delivery.getId(), tenantId))
                .thenReturn(Optional.of(delivery));
        when(deliveryRepository.save(any(UnifiedDelivery.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private FinancialTransactionDto capturedTransaction() {
        ArgumentCaptor<FinancialTransactionDto> captor = ArgumentCaptor.forClass(FinancialTransactionDto.class);
        verify(financialTransactionPort).record(captor.capture());
        return captor.getValue();
    }
}
