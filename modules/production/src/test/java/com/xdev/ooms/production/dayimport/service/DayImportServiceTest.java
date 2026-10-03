package com.xdev.ooms.production.dayimport.service;

import com.xdev.ooms.production.dayimport.model.DayImportWorkbook;
import com.xdev.ooms.production.dayimport.model.DayImportWorkbook.*;
import com.xdev.ooms.production.storageunit.entity.StorageUnit;
import com.xdev.ooms.production.storageunit.repository.StorageUnitRepo;
import com.xdev.ooms.production.unifieddelivery.entity.UnifiedDelivery;
import com.xdev.ooms.production.unifieddelivery.repository.DeliveryRepository;
import com.xdev.ooms.production.unifieddelivery.service.UnifiedDeliveryService;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.LocalDate;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class DayImportServiceTest {
    Map<Class<?>,Object> dependencies;
    DayImportService service;
    DayImportWorkbook workbook;
    <T> T dep(Class<T> type) { return type.cast(dependencies.get(type)); }
    @BeforeEach void setup() throws Exception {
        dependencies = new HashMap<>();
        var ctor=DayImportService.class.getConstructors()[0];
        Object[] args=Arrays.stream(ctor.getParameterTypes()).map(t -> { Object m=mock(t);dependencies.put(t,m);return m; }).toArray();
        service=(DayImportService)ctor.newInstance(args);
        TenantContext.setCurrentTenant(UUID.randomUUID());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("review", "", List.of(new SimpleGrantedAuthority("ADMIN"))));
        workbook=new DayImportWorkbook(); workbook.setBusinessDate(LocalDate.of(2026,9,5)); workbook.setTemplateVersion(2);
        when(dep(DayImportWorkbookReader.class).read(any())).thenReturn(workbook);
        when(dep(DayImportLedger.class).payload(any(), any(Object[].class))).thenReturn("payload");
    }
    @AfterEach void cleanup() { TenantContext.clear(); SecurityContextHolder.clearContext(); }

    @Test void unknownPaymentReferenceBlocksBeforeWrites() throws Exception {
        PaymentRow p=payment();workbook.getPayments().add(p);
        assertFalse(service.dryRun(new byte[0]).isCanCommit());
        assertThrows(DayImportRejectedException.class, () -> service.commit(new byte[0]));
        verifyNoInteractions(dep(UnifiedDeliveryService.class));
    }
    @Test void unauthenticatedAndMissingTenantAreDenied() {
        SecurityContextHolder.clearContext();
        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> service.dryRun(new byte[0]));
        TenantContext.clear();
        assertThrows(org.springframework.security.access.AccessDeniedException.class, DayImportAccess::tenant);
    }
    @Test void receptionPermissionDoesNotGrantPaymentPermission() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("operator", "", List.of(new SimpleGrantedAuthority("RECEPTION:UNIFIEDDELIVERY:CREATE"))));
        workbook.getPayments().add(payment());
        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> service.dryRun(new byte[0]));
    }
    @Test void repeatedPaymentDoesNotCallPostingAgain() throws Exception {
        PaymentRow p=payment();workbook.getPayments().add(p);
        when(dep(DayImportLedger.class).operation("PAYMENT", p.externalRef)).thenReturn("payload");
        var result=service.commit(new byte[0]);
        assertTrue(result.isCanCommit()); assertEquals(1,result.getStatusCounts().get("SKIP_DUPLICATE"));
        verifyNoInteractions(dep(UnifiedDeliveryService.class));
    }
    @Test void changedPaymentIdentityIsRejected() throws Exception {
        workbook.getPayments().add(payment());
        when(dep(DayImportLedger.class).operation("PAYMENT", "P1")).thenReturn("different");
        assertFalse(service.dryRun(new byte[0]).isCanCommit());
    }
    @Test void aggregateTankSalesCannotExceedBalance() throws Exception {
        tank(100d);
        for(int i=0;i<2;i++) { OilSaleRow r=new OilSaleRow();r.externalRef="S"+i;r.quantity=70d;r.unitPrice=1d;r.storageUnitKey="A";workbook.getOilSales().add(r); }
        var result=service.dryRun(new byte[0]);
        assertFalse(result.isCanCommit());assertEquals(1,result.getInvalidCount());
    }
    @Test void sameDayReceiptFundsPlannedSale() throws Exception {
        tank(0d);
        ReceptionRow r=new ReceptionRow();r.externalRef="R1";r.deliveryType="OIL";r.oliveOilType="OC";r.regionName="Tunis";r.oilQuantity=100d;r.unitPrice=1d;r.storageUnitKey="A";workbook.getReceptions().add(r);
        OilSaleRow sale=new OilSaleRow();sale.externalRef="S1";sale.quantity=80d;sale.unitPrice=2d;sale.storageUnitKey="A";workbook.getOilSales().add(sale);
        assertTrue(service.dryRun(new byte[0]).isCanCommit());
    }
    @Test void dryRunShowsTheLotNumbersTheAppWillAssign() throws Exception {
        tank(0d);
        ReceptionRow olive=new ReceptionRow();olive.externalRef="R1";olive.deliveryType="OLIVE";olive.oliveOilType="OB";olive.regionName="Tunis";olive.poidsNet=500d;workbook.getReceptions().add(olive);
        ReceptionRow oil=new ReceptionRow();oil.externalRef="R2";oil.deliveryType="OIL";oil.oliveOilType="HC";oil.regionName="Tunis";oil.oilQuantity=10d;oil.unitPrice=1d;oil.storageUnitKey="A";workbook.getReceptions().add(oil);
        when(dep(UnifiedDeliveryService.class).nextFreeDeliverySequences(2)).thenReturn(List.of(7, 12));
        var rows=service.dryRun(new byte[0]).getRows().stream().filter(r -> "Receptions".equals(r.getSheet())).toList();
        assertEquals(List.of("0007OB26","0012OC26"), rows.stream().map(r -> r.getLotNumber()).toList());
    }
    @Test void legacyPaymentsAndInvalidEnumsAreRejected() throws Exception {
        workbook.setTemplateVersion(1);PaymentRow p=payment();p.externalRef=null;p.paymentMethod="TYPO";workbook.getPayments().add(p);
        assertTrue(service.dryRun(new byte[0]).getInvalidCount() >= 2);
    }
    @Test void duplicateReceptionDoesNotFinalizeExistingState() throws Exception {
        ReceptionRow row=new ReceptionRow();row.externalRef="R1";workbook.getReceptions().add(row);
        UnifiedDelivery existing=new UnifiedDelivery();existing.setId(UUID.randomUUID());existing.setTenantId(TenantContext.getCurrentTenant());
        when(dep(DayImportLedger.class).operation("RECEPTION","R1")).thenReturn("payload");
        when(dep(DeliveryRepository.class).findFirstByTenantIdAndDescriptionContainingIgnoreCaseAndIsDeletedFalse(TenantContext.getCurrentTenant(),"[IMP:R1]")).thenReturn(Optional.of(existing));
        assertTrue(service.commit(new byte[0]).isCanCommit());
        verify(dep(DeliveryRepository.class), never()).save(any());
        verify(dep(DeliveryRepository.class), never()).findFirstByDescriptionContainingIgnoreCaseAndIsDeletedFalse(anyString());
    }
    @Test void pendingReceptionDoesNotHideInvalidQc() throws Exception {
        tank(0d);
        ReceptionRow r=new ReceptionRow();r.externalRef="R1";r.deliveryType="OIL";r.oliveOilType="OC";r.regionName="Tunis";r.oilQuantity=10d;r.unitPrice=1d;r.storageUnitKey="A";workbook.getReceptions().add(r);
        QcResultRow qc=new QcResultRow();qc.receptionExternalRef="R1";qc.ruleKey="missing";qc.value="invalid";workbook.getQcResults().add(qc);
        assertFalse(service.dryRun(new byte[0]).isCanCommit());
    }
    private PaymentRow payment() { PaymentRow p=new PaymentRow();p.externalRef="P1";p.receptionExternalRef="R1";p.amount=30d;return p; }
    private void tank(double volume) {
        StorageUnit tank=new StorageUnit();tank.setId(UUID.randomUUID());tank.setTenantId(TenantContext.getCurrentTenant());tank.setCurrentVolume(volume);tank.setMaxCapacity(1000d);
        when(dep(StorageUnitRepo.class).findFirstByTenantIdAndNameIgnoreCaseAndIsDeletedFalse(TenantContext.getCurrentTenant(),"A")).thenReturn(Optional.of(tank));
        when(dep(StorageUnitRepo.class).findByIdAndTenantIdAndIsDeletedFalse(tank.getId(),TenantContext.getCurrentTenant())).thenReturn(Optional.of(tank));
    }
}
