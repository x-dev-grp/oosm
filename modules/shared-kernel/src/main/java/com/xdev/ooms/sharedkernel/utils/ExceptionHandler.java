package com.xdev.ooms.sharedkernel.utils;

import com.xdev.ooms.sharedkernel.apiDTOs.ApiResponse;
import com.xdev.ooms.sharedkernel.apiDTOs.ApiSingleResponse;
import com.xdev.ooms.sharedkernel.dtos.BaseDto;
import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Centralized exception handler utility for OSM project
 * Provides consistent exception handling with detailed logging and proper error responses
 */
public class ExceptionHandler {

    /**
     * Handle and log exceptions with appropriate HTTP status codes
     */
    public static <E extends BaseEntity, OUTDTO extends BaseDto<E>> ResponseEntity<ApiResponse<E, OUTDTO>> handleException(
            Class<?> clazz,
            String operation,
            Exception exception) {

        return handleException(clazz, operation, exception, null);
    }

    /**
     * Handle and log exceptions with custom error message
     */
    public static <E extends BaseEntity, OUTDTO extends BaseDto<E>> ResponseEntity<ApiResponse<E, OUTDTO>> handleException(
            Class<?> clazz,
            String operation,
            Exception exception,
            String customMessage) {

        // Log the exception with detailed analysis
        OSMLogger.logException(clazz,
                customMessage != null ? customMessage : "Error during " + operation,
                exception,
                determineSeverity(exception));

        // Determine HTTP status and error message
        HttpStatus status = determineHttpStatus(exception);
        String errorMessage = determineErrorMessage(exception, customMessage, operation);

        // Create error response
        ApiResponse<E, OUTDTO> errorResponse = new ApiResponse<>(false, errorMessage, null);

        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Handle and log exceptions for single entity responses
     */
    public static <E extends BaseEntity, OUTDTO extends BaseDto<E>> ResponseEntity<ApiSingleResponse<E, OUTDTO>> handleSingleException(
            Class<?> clazz,
            String operation,
            Exception exception) {

        return handleSingleException(clazz, operation, exception, null);
    }

    /**
     * Handle and log exceptions for single entity responses with custom message
     */
    public static <E extends BaseEntity, OUTDTO extends BaseDto<E>> ResponseEntity<ApiSingleResponse<E, OUTDTO>> handleSingleException(
            Class<?> clazz,
            String operation,
            Exception exception,
            String customMessage) {

        // Log the exception with detailed analysis
        OSMLogger.logException(clazz,
                customMessage != null ? customMessage : "Error during " + operation,
                exception,
                determineSeverity(exception));

        // Determine HTTP status and error message
        HttpStatus status = determineHttpStatus(exception);
        String errorMessage = determineErrorMessage(exception, customMessage, operation);

        // Create error response
        ApiSingleResponse<E, OUTDTO> errorResponse = new ApiSingleResponse<>(false, errorMessage, null);

        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Determine the severity level based on exception type
     */
    private static OSMLogger.ExceptionSeverity determineSeverity(Exception exception) {
        if (exception instanceof AccessDeniedException) {
            return OSMLogger.ExceptionSeverity.HIGH;
        } else if (exception instanceof SQLException) {
            return OSMLogger.ExceptionSeverity.CRITICAL;
        } else if (exception instanceof EntityNotFoundException) {
            return OSMLogger.ExceptionSeverity.LOW;
        } else if (exception instanceof IllegalArgumentException ||
                exception instanceof MethodArgumentTypeMismatchException) {
            return OSMLogger.ExceptionSeverity.MEDIUM;
        } else {
            return OSMLogger.ExceptionSeverity.MEDIUM;
        }
    }

    /**
     * Determine appropriate HTTP status code based on exception type
     */
    private static HttpStatus determineHttpStatus(Exception exception) {
        if (exception instanceof EntityNotFoundException) {
            return HttpStatus.NOT_FOUND;
        } else if (exception instanceof AccessDeniedException) {
            return HttpStatus.FORBIDDEN;
        } else if (exception instanceof IllegalArgumentException ||
                exception instanceof MethodArgumentTypeMismatchException ||
                exception instanceof MethodArgumentNotValidException ||
                exception instanceof BindException ||
                exception instanceof ConstraintViolationException) {
            return HttpStatus.BAD_REQUEST;
        } else if (exception instanceof SQLException) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    /**
     * Determine appropriate error message based on exception type
     */
    private static String determineErrorMessage(Exception exception, String customMessage, String operation) {
        if (customMessage != null) {
            return customMessage;
        }

        if (exception instanceof EntityNotFoundException) {
            return "Requested resource not found";
        } else if (exception instanceof AccessDeniedException) {
            return "Access denied: insufficient permissions";
        } else if (exception instanceof IllegalArgumentException) {
            return "Invalid argument provided: " + exception.getMessage();
        } else if (exception instanceof MethodArgumentTypeMismatchException ex) {
            return String.format("Invalid parameter type for '%s': expected %s, got %s",
                    ex.getName(), ex.getRequiredType().getSimpleName(),
                    ex.getValue() != null ? ex.getValue().getClass().getSimpleName() : "null");
        } else if (exception instanceof MethodArgumentNotValidException) {
            return extractValidationErrors((MethodArgumentNotValidException) exception);
        } else if (exception instanceof BindException) {
            return extractBindingErrors((BindException) exception);
        } else if (exception instanceof ConstraintViolationException) {
            return extractConstraintViolations((ConstraintViolationException) exception);
        } else if (exception instanceof SQLException) {
            return "Database operation failed";
        } else {
            return "An unexpected error occurred during " + operation;
        }
    }

    /**
     * Extract validation errors from MethodArgumentNotValidException
     */
    private static String extractValidationErrors(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return "Validation failed: " + errors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));
    }

    /**
     * Extract binding errors from BindException
     */
    private static String extractBindingErrors(BindException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return "Binding failed: " + errors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));
    }

    /**
     * Extract constraint violations from ConstraintViolationException
     */
    private static String extractConstraintViolations(ConstraintViolationException exception) {
        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
        return "Constraint violations: " + violations.stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
    }

    /**
     * Create a generic error response
     */
    public static <E extends BaseEntity, OUTDTO extends BaseDto<E>> ApiResponse<E, OUTDTO> createErrorResponse(String message) {
        return new ApiResponse<>(false, message, null);
    }

    /**
     * Create a generic success response
     */
    public static <E extends BaseEntity, OUTDTO extends BaseDto<E>> ApiResponse<E, OUTDTO> createSuccessResponse(String message, java.util.List<OUTDTO> data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Create a generic single error response
     */
    public static <E extends BaseEntity, OUTDTO extends BaseDto<E>> ApiSingleResponse<E, OUTDTO> createSingleErrorResponse(String message) {
        return new ApiSingleResponse<>(false, message, null);
    }

    /**
     * Create a generic single success response
     */
    public static <E extends BaseEntity, OUTDTO extends BaseDto<E>> ApiSingleResponse<E, OUTDTO> createSingleSuccessResponse(String message, OUTDTO data) {
        return new ApiSingleResponse<>(true, message, data);
    }
} 