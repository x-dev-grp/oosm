package com.xdev.ooms.sharedkernel.utils;

import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TenantAccessTest {

    private final UUID tenant = UUID.randomUUID();

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void ownRowIsAccessible() {
        TenantContext.setCurrentTenant(tenant);
        assertTrue(TenantAccess.isAccessible(entity(tenant)));
    }

    @Test
    void rowOfAnotherTenantIsNotAccessible() {
        TenantContext.setCurrentTenant(tenant);
        assertFalse(TenantAccess.isAccessible(entity(UUID.randomUUID())));
    }

    @Test
    void sharedReferenceRowIsAccessible() {
        TenantContext.setCurrentTenant(tenant);
        assertTrue(TenantAccess.isAccessible(entity(null)));
    }

    @Test
    void callerWithoutTenantSeesEverything() {
        assertTrue(TenantAccess.isAccessible(entity(UUID.randomUUID())));
    }

    @Test
    void nullEntityIsNotAccessible() {
        assertFalse(TenantAccess.isAccessible(null));
    }

    @Test
    void requireReturnsOwnRowAndHidesOthers() {
        TenantContext.setCurrentTenant(tenant);
        BaseEntity own = entity(tenant);
        UUID id = UUID.randomUUID();

        assertSame(own, TenantAccess.require(Optional.of(own), "Cuve", id));
        assertThrows(EntityNotFoundException.class,
                () -> TenantAccess.require(Optional.of(entity(UUID.randomUUID())), "Cuve", id));
        assertThrows(EntityNotFoundException.class,
                () -> TenantAccess.require(Optional.empty(), "Cuve", id));
    }

    private static BaseEntity entity(UUID owner) {
        BaseEntity entity = new BaseEntity();
        entity.setTenantId(owner);
        return entity;
    }
}
