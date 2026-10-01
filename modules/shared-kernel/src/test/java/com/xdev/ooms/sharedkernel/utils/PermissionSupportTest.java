package com.xdev.ooms.sharedkernel.utils;

import com.xdev.ooms.sharedkernel.models.Action;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionSupportTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void grantsActionPresentForResource() {
        authenticate("PRODUCTION:STORAGEUNIT:UPDATE");

        assertTrue(PermissionSupport.hasAction("STORAGEUNIT", Action.UPDATE));
        assertDoesNotThrow(() -> PermissionSupport.requireAction("STORAGEUNIT", Action.UPDATE));
    }

    @Test
    void refusesActionMissingForResource() {
        authenticate("PRODUCTION:STORAGEUNIT:READ");

        assertFalse(PermissionSupport.hasAction("STORAGEUNIT", Action.DELETE));
        assertThrows(AccessDeniedException.class, () -> PermissionSupport.requireAction("STORAGEUNIT", Action.DELETE));
    }

    @Test
    void actionOnAnotherResourceDoesNotLeak() {
        authenticate("PRODUCTION:OILTRANSACTION:VALIDATE");

        assertFalse(PermissionSupport.hasAction("STORAGEUNIT", Action.VALIDATE));
    }

    @Test
    void resourceNameMustMatchWholeSegment() {
        authenticate("PRODUCTION:STORAGEUNITTYPE:DELETE");

        assertFalse(PermissionSupport.hasAction("STORAGEUNIT", Action.DELETE));
    }

    @Test
    void adminRolesBypassResourceChecks() {
        authenticate("ROLE_ADMIN");
        assertTrue(PermissionSupport.hasAction("STORAGEUNIT", Action.DELETE));

        authenticate("OOSMADMIN");
        assertTrue(PermissionSupport.hasAction("FILTRATIONOPERATION", Action.VALIDATE));
    }

    @Test
    void anonymousCallerHasNoAction() {
        assertFalse(PermissionSupport.hasAction("STORAGEUNIT", Action.READ));
        assertThrows(AccessDeniedException.class, () -> PermissionSupport.requireAction("STORAGEUNIT", Action.READ));
    }

    private static void authenticate(String... authorities) {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user", "n/a", authorities));
    }
}
