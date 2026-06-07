package com.xdev.ooms.sharedkernel.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Comprehensive logging utility for OSM project with detailed exception handling
 * and stack trace analysis to pinpoint exact line where exceptions occur.
 */
public class OSMLogger {

    private static final ConcurrentMap<String, Logger> loggerCache = new ConcurrentHashMap<>();
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Get or create a logger for the specified class
     */
    public static Logger getLogger(Class<?> clazz) {
        return loggerCache.computeIfAbsent(clazz.getName(), LoggerFactory::getLogger);
    }

    /**
     * Get or create a logger for the specified name
     */
    public static Logger getLogger(String name) {
        return loggerCache.computeIfAbsent(name, LoggerFactory::getLogger);
    }

    /**
     * Log an exception with detailed analysis and stack trace
     */
    public static void logException(Class<?> clazz, String message, Throwable exception) {
        logException(clazz, message, exception, ExceptionSeverity.MEDIUM);
    }

    /**
     * Log an exception with detailed analysis, stack trace, and severity level
     */
    public static void logException(Class<?> clazz, String message, Throwable exception, ExceptionSeverity severity) {
        Logger logger = getLogger(clazz);
        String correlationId = generateCorrelationId();

        try {
            // Set correlation ID for tracking
            MDC.put("correlationId", correlationId);
            MDC.put("severity", severity.name());
            MDC.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER));

            // Build detailed exception report
            StringBuilder report = new StringBuilder();
            report.append("\n=== EXCEPTION REPORT ===\n");
            report.append("Correlation ID: ").append(correlationId).append("\n");
            report.append("Timestamp: ").append(LocalDateTime.now().format(TIMESTAMP_FORMATTER)).append("\n");
            report.append("Class: ").append(clazz.getName()).append("\n");
            report.append("Message: ").append(message).append("\n");
            report.append("Exception Type: ").append(exception.getClass().getSimpleName()).append("\n");
            report.append("Exception Message: ").append(exception.getMessage()).append("\n");
            report.append("Severity: ").append(severity.name()).append("\n");

            // Analyze stack trace to find the exact line
            StackTraceElement[] stackTrace = exception.getStackTrace();
            if (stackTrace.length > 0) {
                report.append("\n=== STACK TRACE ANALYSIS ===\n");
                report.append("Primary Exception Location:\n");
                report.append("  File: ").append(stackTrace[0].getFileName()).append("\n");
                report.append("  Class: ").append(stackTrace[0].getClassName()).append("\n");
                report.append("  Method: ").append(stackTrace[0].getMethodName()).append("\n");
                report.append("  Line: ").append(stackTrace[0].getLineNumber()).append("\n");

                // Find the first line in our application code
                StackTraceElement applicationFrame = findApplicationFrame(stackTrace);
                if (applicationFrame != null && !applicationFrame.equals(stackTrace[0])) {
                    report.append("\nApplication Code Location:\n");
                    report.append("  File: ").append(applicationFrame.getFileName()).append("\n");
                    report.append("  Class: ").append(applicationFrame.getClassName()).append("\n");
                    report.append("  Method: ").append(applicationFrame.getMethodName()).append("\n");
                    report.append("  Line: ").append(applicationFrame.getLineNumber()).append("\n");
                }

                // Show relevant stack trace (first 10 frames)
                report.append("\nRelevant Stack Trace:\n");
                int maxFrames = Math.min(10, stackTrace.length);
                for (int i = 0; i < maxFrames; i++) {
                    StackTraceElement element = stackTrace[i];
                    report.append("  ").append(i + 1).append(". ")
                            .append(element.getClassName()).append(".")
                            .append(element.getMethodName())
                            .append("(").append(element.getFileName()).append(":")
                            .append(element.getLineNumber()).append(")\n");
                }
            }

            // Add full stack trace for debugging
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            report.append("\n=== FULL STACK TRACE ===\n").append(sw);

            // Add context information
            report.append("\n=== CONTEXT INFORMATION ===\n");
            report.append("Thread: ").append(Thread.currentThread().getName()).append("\n");
            report.append("Thread ID: ").append(Thread.currentThread().getId()).append("\n");

            // Log based on severity
            switch (severity) {
                case LOW:
                    logger.warn(report.toString());
                    break;
                case MEDIUM:
                    logger.error(report.toString());
                    break;
                case HIGH:
                    logger.error(report.toString());
                    break;
                case CRITICAL:
                    logger.error("🚨 CRITICAL EXCEPTION 🚨\n" + report);
                    break;
            }

        } finally {
            // Clean up MDC
            MDC.clear();
        }
    }

    /**
     * Log a method entry with parameters
     */
    public static void logMethodEntry(Class<?> clazz, String methodName, Object... parameters) {
        Logger logger = getLogger(clazz);
        if (logger.isDebugEnabled()) {
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("→ ENTERING: ").append(methodName);

            if (parameters != null && parameters.length > 0) {
                logMessage.append(" with parameters: ");
                for (int i = 0; i < parameters.length; i++) {
                    if (i > 0) logMessage.append(", ");
                    logMessage.append("param").append(i + 1).append("=");
                    if (parameters[i] != null) {
                        logMessage.append(parameters[i].toString());
                    } else {
                        logMessage.append("null");
                    }
                }
            }

            logger.debug(logMessage.toString());
        }
    }

    /**
     * Log a method exit with return value
     */
    public static void logMethodExit(Class<?> clazz, String methodName, Object returnValue) {
        Logger logger = getLogger(clazz);
        if (logger.isDebugEnabled()) {
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("← EXITING: ").append(methodName);

            if (returnValue != null) {
                logMessage.append(" returning: ").append(returnValue);
            } else {
                logMessage.append(" returning: null");
            }

            logger.debug(logMessage.toString());
        }
    }

    /**
     * Log a method exit without return value
     */
    public static void logMethodExit(Class<?> clazz, String methodName) {
        Logger logger = getLogger(clazz);
        if (logger.isDebugEnabled()) {
            logger.debug("← EXITING: " + methodName);
        }
    }

    /**
     * Log performance metrics
     */
    public static void logPerformance(Class<?> clazz, String operation, long startTime, long endTime) {
        Logger logger = getLogger(clazz);
        long duration = endTime - startTime;

        if (duration > 1000) { // Log warnings for operations taking more than 1 second
            logger.warn("⚠️ SLOW OPERATION: {} took {}ms", operation, duration);
        } else if (duration > 500) { // Log info for operations taking more than 500ms
            logger.info("⏱️ OPERATION: {} took {}ms", operation, duration);
        } else {
            logger.debug("⚡ OPERATION: {} took {}ms", operation, duration);
        }
    }

    /**
     * Log business events
     */
    public static void logBusinessEvent(Class<?> clazz, String event, String details) {
        Logger logger = getLogger(clazz);
        logger.info("📋 BUSINESS EVENT: {} - {}", event, details);
    }

    /**
     * Log security events
     */
    public static void logSecurityEvent(Class<?> clazz, String event, String details) {
        Logger logger = getLogger(clazz);
        logger.warn("🔒 SECURITY EVENT: {} - {}", event, details);
    }

    /**
     * Log data access events
     */
    public static void logDataAccess(Class<?> clazz, String operation, String entity) {
        Logger logger = getLogger(clazz);
        logger.debug("💾 DATA ACCESS: {}   with ID: {}", operation, entity);
    }

    /**
     * Find the first stack frame that belongs to our application code
     */
    private static StackTraceElement findApplicationFrame(StackTraceElement[] stackTrace) {
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            // Check if it's our application code (not framework or library code)
            if (className.startsWith("com.xdev.") || className.startsWith("com.osm.")) {
                return element;
            }
        }
        return null;
    }

    /**
     * Generate a unique correlation ID for tracking requests
     */
    private static String generateCorrelationId() {
        return "OSM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Log with custom level and context
     */
    public static void log(Class<?> clazz, LogLevel level, String message, Object... args) {
        Logger logger = getLogger(clazz);
        String formattedMessage = String.format(message, args);

        switch (level) {
            case TRACE:
                logger.trace(formattedMessage);
                break;
            case DEBUG:
                logger.debug(formattedMessage);
                break;
            case INFO:
                logger.info(formattedMessage);
                break;
            case WARN:
                logger.warn(formattedMessage);
                break;
            case ERROR:
                logger.error(formattedMessage);
                break;
            case FATAL:
                logger.error("💥 FATAL: " + formattedMessage);
                break;
        }
    }

    /**
     * Log with context information
     */
    public static void logWithContext(Class<?> clazz, LogLevel level, String message, String context, Object... args) {
        String correlationId = generateCorrelationId();
        MDC.put("correlationId", correlationId);
        MDC.put("context", context);

        try {
            log(clazz, level, "[{}] " + message, context, args);
        } finally {
            MDC.clear();
        }
    }

    // Log levels
    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR, FATAL
    }

    // Exception severity levels
    public enum ExceptionSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
} 