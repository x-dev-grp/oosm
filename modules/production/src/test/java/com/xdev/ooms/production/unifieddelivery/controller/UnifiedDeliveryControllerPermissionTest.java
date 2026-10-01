package com.xdev.ooms.production.unifieddelivery.controller;

import com.xdev.ooms.production.unifieddelivery.service.UnifiedDeliveryService;
import com.xdev.ooms.sharedkernel.models.Action;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class UnifiedDeliveryControllerPermissionTest {

    private final Probe controller = new Probe(mock(UnifiedDeliveryService.class));

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void qualityControlUserCanReadAndSaveThroughUpdate() {
        authenticate("PRODUCTION:UNIFIEDDELIVERY:OLIVE_QUALITY");

        assertDoesNotThrow(() -> controller.check(Action.READ));
        assertDoesNotThrow(() -> controller.check(Action.UPDATE));
    }

    @Test
    void qualityControlUserCannotCreateOrDelete() {
        authenticate("PRODUCTION:UNIFIEDDELIVERY:OLIVE_QUALITY");

        assertThrows(AccessDeniedException.class, () -> controller.check(Action.CREATE));
        assertThrows(AccessDeniedException.class, () -> controller.check(Action.DELETE));
    }

    @Test
    void planningUserCanReadButNotUpdate() {
        authenticate("PRODUCTION:UNIFIEDDELIVERY:PLANNING");

        assertDoesNotThrow(() -> controller.check(Action.READ));
        assertThrows(AccessDeniedException.class, () -> controller.check(Action.UPDATE));
    }

    @Test
    void permissionOnAnotherResourceGrantsNothing() {
        authenticate("PRODUCTION:STORAGEUNIT:UPDATE");

        assertThrows(AccessDeniedException.class, () -> controller.check(Action.UPDATE));
    }

    private static void authenticate(String... authorities) {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user", "n/a", authorities));
    }

    private static final class Probe extends UnifiedDeliveryController {
        Probe(UnifiedDeliveryService service) {
            super(service, new ModelMapper(), service, service);
        }

        void check(Action action) {
            authorize(action);
        }
    }
}
