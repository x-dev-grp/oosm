package com.xdev.ooms.sharedkernel.services.impl;

import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import com.xdev.ooms.sharedkernel.qr.CodeGenerator;
import com.xdev.ooms.sharedkernel.qr.Component.QrConfig;
import com.xdev.ooms.sharedkernel.repos.BaseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BaseServiceQrTenantIsolationTest {

    private final UUID tenantId = UUID.randomUUID();
    private BaseRepository<TestEntity> repository;
    private TestService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        repository = mock(BaseRepository.class);
        service = new TestService(repository, mock(CodeGenerator.class), mock(QrConfig.class));
        TenantContext.setCurrentTenant(tenantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void resolveHidesQrOwnedByAnotherTenant() {
        TestEntity foreignEntity = entity(UUID.randomUUID(), "ABC123");
        when(repository.findByQrHexIgnoreCaseAndIsDeletedFalse("ABC123"))
                .thenReturn(Optional.of(foreignEntity));

        assertThrows(EntityNotFoundException.class, () -> service.resolve("abc123"));
        assertTrue(service.searchByCode("abc123").isEmpty());
    }

    @Test
    void resolveReturnsQrOwnedByCurrentTenant() {
        TestEntity ownedEntity = entity(tenantId, "ABC123");
        when(repository.findByQrHexIgnoreCaseAndIsDeletedFalse("ABC123"))
                .thenReturn(Optional.of(ownedEntity));

        assertEquals(ownedEntity.getId().toString(), service.resolve("abc123").getEntityId());
        assertEquals("ABC123", service.resolve("abc123").getPublicCode());
    }

    @Test
    void directQrOperationsHideEntityOwnedByAnotherTenant() {
        TestEntity foreignEntity = entity(UUID.randomUUID(), "ABC123");
        when(repository.findByIdAndIsDeletedFalse(foreignEntity.getId()))
                .thenReturn(Optional.of(foreignEntity));
        when(repository.findByQrHexIgnoreCaseAndIsDeletedFalse("ABC123"))
                .thenReturn(Optional.of(foreignEntity));

        assertThrows(EntityNotFoundException.class, () -> service.getEntityById(foreignEntity.getId()));
        assertThrows(EntityNotFoundException.class,
                () -> service.generateQrInfo("TEST", foreignEntity.getId(), true));
        assertThrows(EntityNotFoundException.class, () -> service.generateQrImage("ABC123"));
        assertThrows(EntityNotFoundException.class, () -> service.generateQrImageFromEntity(foreignEntity));

        verify(repository).findByQrHexIgnoreCaseAndIsDeletedFalse("ABC123");
    }

    private static TestEntity entity(UUID owner, String qrHex) {
        TestEntity entity = new TestEntity();
        entity.setId(UUID.randomUUID());
        entity.setTenantId(owner);
        entity.setQrHex(qrHex);
        return entity;
    }

    private static final class TestEntity extends BaseEntity {
    }

    private static final class TestDto extends BaseDto<TestEntity> {
    }

    private static final class TestService extends BaseServiceImpl<TestEntity, TestDto, TestDto> {
        private TestService(BaseRepository<TestEntity> repository, CodeGenerator codeGenerator, QrConfig qrConfig) {
            super(repository, codeGenerator, qrConfig, new ModelMapper());
        }

        @Override
        protected String getEntityType() {
            return "TEST";
        }

        @Override
        protected String getLabel(TestEntity entity) {
            return "Test";
        }

        @Override
        protected String getStatus(TestEntity entity) {
            return "ACTIVE";
        }

        @Override
        protected String getMobileRoute() {
            return "/test";
        }
    }
}
