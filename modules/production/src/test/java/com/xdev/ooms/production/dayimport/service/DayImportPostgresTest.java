package com.xdev.ooms.production.dayimport.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xdev.ooms.production.dayimport.model.DayImportWorkbook;
import com.xdev.ooms.production.dayimport.model.DayImportWorkbook.*;
import com.xdev.ooms.sharedkernel.basetype.dto.BaseTypeDto;
import com.xdev.ooms.sharedkernel.basetype.service.GenericTypeService;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.ports.ExpensePort;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.*;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import javax.sql.DataSource;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/** Opt-in disposable PostgreSQL only. CI supplies a dedicated database, never application properties. */
@SpringJUnitConfig(DayImportPostgresTest.Config.class)
@EnabledIfEnvironmentVariable(named="DAY_IMPORT_TEST_JDBC_URL", matches=".+")
class DayImportPostgresTest {
    static final Map<Class<?>,Object> mocks=new HashMap<>();
    static <T> T dep(Class<T> type) { return type.cast(mocks.get(type)); }
    @Autowired DayImportWorkflow workflow;
    @Autowired DayImportLedger ledger;
    @Autowired DayImportDriveStore driveStore;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager manager;
    DayImportWorkbook workbook;
    @Configuration @EnableTransactionManagement static class Config {
        @Bean DataSource dataSource() {
            String url=System.getenv("DAY_IMPORT_TEST_JDBC_URL");
            if (!url.contains("day_import_review_test")) throw new IllegalArgumentException("Dedicated day_import_review_test database required");
            PGSimpleDataSource ds=new PGSimpleDataSource();ds.setURL(url);ds.setUser(System.getenv().getOrDefault("DAY_IMPORT_TEST_USER","postgres"));ds.setPassword(System.getenv().getOrDefault("DAY_IMPORT_TEST_PASSWORD",""));
            String schema="review_"+UUID.randomUUID().toString().replace("-", "");
            new JdbcTemplate(ds).execute("CREATE SCHEMA " + schema);ds.setCurrentSchema(schema);
            var populator=new ResourceDatabasePopulator(new FileSystemResource(Path.of("../../app/src/main/resources/db/day-import-schema.sql")));
            populator.execute(ds);
            // Re-running the additive schema must be safe.
            populator.execute(ds);
            new JdbcTemplate(ds).execute("CREATE TABLE review_effect (tenant_id UUID NOT NULL, kind TEXT NOT NULL)");
            return ds;
        }
        @Bean JdbcTemplate jdbc(DataSource ds) { return new JdbcTemplate(ds); }
        @Bean PlatformTransactionManager manager(DataSource ds) { return new DataSourceTransactionManager(ds); }
        @Bean ObjectMapper mapper() { return new ObjectMapper().findAndRegisterModules(); }
        @Bean DayImportLedger ledger(JdbcTemplate j,ObjectMapper m) { return new DayImportLedger(j,m); }
        @Bean DayImportDriveStore driveStore(JdbcTemplate j,ObjectMapper m) { return new DayImportDriveStore(j,m); }
        @Bean DayImportService engine(DayImportLedger ledger) throws Exception {
            var ctor=DayImportService.class.getConstructors()[0];
            Object[] args=Arrays.stream(ctor.getParameterTypes()).map(t->{if(t==DayImportLedger.class)return ledger;Object m=mock(t);mocks.put(t,m);return m;}).toArray();
            return (DayImportService)ctor.newInstance(args);
        }
        @Bean DayImportWorkflow workflow(DayImportService engine,DayImportLedger ledger,PlatformTransactionManager manager) { return new DayImportWorkflow(engine,ledger,manager); }
    }
    @BeforeEach void setup() throws Exception {
        mocks.values().forEach(org.mockito.Mockito::reset);
        TenantContext.setCurrentTenant(UUID.randomUUID());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("review", "", List.of(new SimpleGrantedAuthority("ADMIN"))));
        workbook=new DayImportWorkbook();workbook.setBusinessDate(LocalDate.of(2026,9,5));workbook.setTemplateVersion(2);
        when(dep(DayImportWorkbookReader.class).read(any())).thenReturn(workbook);
    }
    @AfterEach void cleanup() { TenantContext.clear();SecurityContextHolder.clearContext(); }

    @Test void lateFailureRollsBackBusinessWritesAndRetainsRejectedRun() throws Exception {
        NamedRow region=new NamedRow();region.name="new";workbook.getRegions().add(region);
        ExpenseRow expense=new ExpenseRow();expense.externalRef="E1";expense.amount=10d;workbook.getExpenses().add(expense);
        when(dep(GenericTypeService.class).save(any(BaseTypeDto.class))).thenAnswer(inv->{jdbc.update("INSERT INTO review_effect VALUES (?,?)",TenantContext.getCurrentTenant(),"REGION");BaseTypeDto dto=inv.getArgument(0);dto.setId(UUID.randomUUID());return dto;});
        when(dep(ExpensePort.class).record(any())).thenThrow(new IllegalStateException("Injected late failure"));
        var preview=workflow.preview(new byte[]{1},"TEST");assertTrue(preview.isCanCommit());
        assertThrows(DayImportRejectedException.class,()->workflow.commit(new byte[]{1},preview.getRunId()));
        assertEquals(0,jdbc.queryForObject("SELECT count(*) FROM review_effect WHERE tenant_id=?",Integer.class,TenantContext.getCurrentTenant()));
        assertEquals("REJECTED",workflow.report(preview.getRunId()).getOutcome());
        assertEquals(0,jdbc.queryForObject("SELECT count(*) FROM day_import_operation WHERE tenant_id=?",Integer.class,TenantContext.getCurrentTenant()));
    }
    @Test void repeatedFileReturnsSavedResultAndPostsOnce() throws Exception {
        ExpenseRow expense=new ExpenseRow();expense.externalRef="E1";expense.amount=10d;workbook.getExpenses().add(expense);
        var preview=workflow.preview(new byte[]{2},"TEST");
        assertEquals("COMMITTED",workflow.commit(new byte[]{2},preview.getRunId()).getOutcome());
        assertEquals("REPLAYED",workflow.commit(new byte[]{2},preview.getRunId()).getOutcome());
        verify(dep(ExpensePort.class),times(1)).record(any());
    }
    @Test void fileMismatchAndForeignRunAreDenied() throws Exception {
        var preview=workflow.preview(new byte[]{3},"TEST");
        assertThrows(DayImportRejectedException.class,()->workflow.commit(new byte[]{4},preview.getRunId()));
        TenantContext.setCurrentTenant(UUID.randomUUID());
        assertThrows(IllegalArgumentException.class,()->workflow.report(preview.getRunId()));
    }
    @Test void databaseUniqueIdentityIsTenantScoped() {
        UUID tenant=TenantContext.getCurrentTenant();
        new TransactionTemplate(manager).execute(s->{ledger.record("PAYMENT","P1","same",null);return null;});
        assertThrows(org.springframework.dao.DuplicateKeyException.class,()->new TransactionTemplate(manager).execute(s->{ledger.record("PAYMENT","p1","same",null);return null;}));
        TenantContext.setCurrentTenant(UUID.randomUUID());ledger.record("PAYMENT","P1","other",null);
        assertEquals("other",ledger.operation("PAYMENT","P1"));
        TenantContext.setCurrentTenant(tenant);assertEquals("same",ledger.operation("PAYMENT","P1"));
    }
    @Test void databaseLeaseAndDriveStatusAreIsolated() {
        UUID tenant=TenantContext.getCurrentTenant();UUID lease=driveStore.acquire();assertNotNull(lease);assertNull(driveStore.acquire());
        var status=new com.xdev.ooms.production.dayimport.dto.DayImportDriveStatusDto();status.setLastError("tenant A file");status.setLastSyncAt(java.time.Instant.now());
        driveStore.finish(lease,status);assertEquals("tenant A file",driveStore.status().getLastError());
        TenantContext.setCurrentTenant(UUID.randomUUID());assertNull(driveStore.status().getLastError());assertNotNull(driveStore.acquire());
        TenantContext.setCurrentTenant(tenant);assertNotNull(driveStore.acquire());
    }
    @Test void concurrentClaimsSerializeThroughTransactionLock() throws Exception {
        UUID tenant=TenantContext.getCurrentTenant();var pool=Executors.newFixedThreadPool(2);var start=new CountDownLatch(1);
        Callable<Boolean> claim=()->{TenantContext.setCurrentTenant(tenant);try{start.await();return new TransactionTemplate(manager).execute(s->{ledger.lockTenant();if(ledger.operation("PAYMENT","P2")!=null)return false;ledger.record("PAYMENT","P2","same",null);return true;});}finally{TenantContext.clear();}};
        try {var first=pool.submit(claim);var second=pool.submit(claim);start.countDown();assertNotEquals(first.get(10,TimeUnit.SECONDS),second.get(10,TimeUnit.SECONDS));}
        finally {pool.shutdownNow();}
    }
}
