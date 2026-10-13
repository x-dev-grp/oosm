package com.xdev.ooms.production.dayimport.service;

import com.xdev.ooms.production.storageunit.entity.StorageUnit;
import com.xdev.ooms.production.storageunit.repository.StorageUnitRepo;
import com.xdev.ooms.production.unifieddelivery.repository.DeliveryRepository;
import com.xdev.ooms.production.oilsale.repository.OilSaleRepository;
import com.xdev.ooms.production.supplier.repository.SupplierRepository;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Exercises the real domain mappings and repository predicates on a disposable schema. */
@EnabledIfEnvironmentVariable(named="DAY_IMPORT_TEST_JDBC_URL", matches=".+")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DayImportJpaTest {
    LocalContainerEntityManagerFactoryBean factory;
    EntityManagerFactory emf;
    @BeforeAll void setup() {
        String url=System.getenv("DAY_IMPORT_TEST_JDBC_URL");
        if (!url.contains("day_import_review_test")) throw new IllegalArgumentException("Dedicated test database required");
        var ds=new PGSimpleDataSource();ds.setURL(url);ds.setUser(System.getenv().getOrDefault("DAY_IMPORT_TEST_USER","postgres"));ds.setPassword(System.getenv().getOrDefault("DAY_IMPORT_TEST_PASSWORD",""));
        String schema="jpa_review_"+UUID.randomUUID().toString().replace("-", "");
        new JdbcTemplate(ds).execute("CREATE SCHEMA " + schema);ds.setCurrentSchema(schema);
        factory=new LocalContainerEntityManagerFactoryBean();factory.setDataSource(ds);factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setPackagesToScan("com.xdev.ooms.production", "com.xdev.ooms.inventory", "com.xdev.ooms.sharedkernel");
        factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto","create-drop", "hibernate.default_schema",schema,
                "hibernate.cache.use_second_level_cache","false", "hibernate.physical_naming_strategy","org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy"));
        factory.afterPropertiesSet();emf=factory.getObject();
    }
    @AfterAll void close() { if(factory!=null)factory.destroy();TenantContext.clear(); }
    @Test void realRepositoryQueriesScopeSameNamedTanksByTenant() {
        UUID a=UUID.randomUUID(),b=UUID.randomUUID();
        try(var em=emf.createEntityManager()) {
            em.getTransaction().begin();StorageUnit first=tank(a),second=tank(b);em.persist(first);em.persist(second);em.getTransaction().commit();em.clear();
            var repositories=new JpaRepositoryFactory(em);
            var tanks=repositories.getRepository(StorageUnitRepo.class);
            assertEquals(first.getId(),tanks.findFirstByTenantIdAndNameIgnoreCaseAndIsDeletedFalse(a,"shared-name").orElseThrow().getId());
            assertEquals(second.getId(),tanks.findFirstByTenantIdAndNameIgnoreCaseAndIsDeletedFalse(b,"shared-name").orElseThrow().getId());
            assertTrue(tanks.findByIdAndTenantIdAndIsDeletedFalse(first.getId(),b).isEmpty());
            // Parse all new derived queries against the actual entity metadata.
            repositories.getRepository(DeliveryRepository.class);
            repositories.getRepository(OilSaleRepository.class);
            repositories.getRepository(SupplierRepository.class);
        }
    }
    @Test void competingDomainWritersCannotOverwriteStockWithStaleBalance() {
        UUID id;
        try(var em=emf.createEntityManager()) {em.getTransaction().begin();var tank=tank(UUID.randomUUID());em.persist(tank);em.getTransaction().commit();id=tank.getId();}
        try(var first=emf.createEntityManager();var second=emf.createEntityManager()) {
            first.getTransaction().begin();second.getTransaction().begin();
            var a=first.find(StorageUnit.class,id);var b=second.find(StorageUnit.class,id);
            a.setCurrentVolume(30d);first.getTransaction().commit();b.setCurrentVolume(20d);
            assertThrows(RollbackException.class,()->second.getTransaction().commit());
        }
        try(var em=emf.createEntityManager()) {assertEquals(30d,em.find(StorageUnit.class,id).getCurrentVolume());}
    }
    private StorageUnit tank(UUID tenant) {
        var tank=new StorageUnit();tank.setTenantId(tenant);tank.setName("shared-name");tank.setCurrentVolume(100d);tank.setMaxCapacity(1000d);return tank;
    }
}
