package com.xdev.ooms.production.unifieddelivery.controller;

import com.xdev.ooms.production.unifieddelivery.service.UnifiedDeliveryService;
import com.xdev.ooms.sharedkernel.models.Action;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Test
    void retainedMutationRoutesAcceptGetAndPost() throws Exception {
        assertGetAndPost("updateStatue", UUID.class,
                com.xdev.ooms.sharedkernel.Enum.OliveLotStatus.class, String.class);
        assertGetAndPost("updatePrice", UUID.class, Double.class);
        assertGetAndPost("createOilRecFromOliveRec", UUID.class);
    }

    @Test
    void retainedOilConversionRouteHasNoAdditionalControllerPermissionGate() {
        SecurityContextHolder.clearContext();

        assertDoesNotThrow(() -> controller.createOilRecFromOliveRec(UUID.randomUUID()));
    }

    private static void assertGetAndPost(String methodName, Class<?>... parameterTypes) throws Exception {
        Method method = UnifiedDeliveryController.class.getMethod(methodName, parameterTypes);
        RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);

        assertNotNull(mapping);
        assertEquals(Set.of(RequestMethod.GET, RequestMethod.POST), Set.of(mapping.method()));
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
