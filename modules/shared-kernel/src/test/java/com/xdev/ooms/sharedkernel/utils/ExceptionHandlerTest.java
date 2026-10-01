package com.xdev.ooms.sharedkernel.utils;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ExceptionHandlerTest {

    @Test
    void businessRefusalIsConflictWithItsMessage() {
        var response = ExceptionHandler.handleException(getClass(), "delete", new IllegalStateException("Cuve non vide"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Cuve non vide", response.getBody().getMessage());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void unsupportedOperationIsConflict() {
        assertEquals(HttpStatus.CONFLICT, status(new UnsupportedOperationException("Lecture seule")));
    }

    @Test
    void concurrentBalanceUpdateIsConflictAskingToRetry() {
        var response = ExceptionHandler.handleException(getClass(), "approve",
                new ObjectOptimisticLockingFailureException("StorageUnit", "id"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Les données ont été modifiées par un autre utilisateur, réessayez", response.getBody().getMessage());
    }

    @Test
    void missingPermissionIsForbidden() {
        assertEquals(HttpStatus.FORBIDDEN, status(new AccessDeniedException("Missing permission")));
    }

    @Test
    void invalidInputIsBadRequest() {
        assertEquals(HttpStatus.BAD_REQUEST, status(new IllegalArgumentException("Quantite invalide")));
    }

    @Test
    void missingEntityIsNotFound() {
        assertEquals(HttpStatus.NOT_FOUND, status(new EntityNotFoundException("absent")));
    }

    @Test
    void unexpectedErrorIsServerErrorWithoutInternalDetails() {
        var response = ExceptionHandler.handleException(getClass(), "update", new RuntimeException("SQL secret detail"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred during update", response.getBody().getMessage());
    }

    private HttpStatus status(Exception exception) {
        return (HttpStatus) ExceptionHandler.handleException(getClass(), "op", exception).getStatusCode();
    }
}
