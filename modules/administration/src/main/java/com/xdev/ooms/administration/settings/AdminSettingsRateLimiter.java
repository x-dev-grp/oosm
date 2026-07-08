package com.xdev.ooms.administration.settings;

import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AdminSettingsRateLimiter {

    private final int mailTestLimit;
    private final Duration mailTestWindow;
    private final int secretRotationLimit;
    private final Duration secretRotationWindow;
    private final boolean countFailedMailTests;

    private final Map<String, List<Instant>> mailTests = new ConcurrentHashMap<>();
    private final Map<String, List<Instant>> secretRotations = new ConcurrentHashMap<>();

    public AdminSettingsRateLimiter(
            @Value("${app.settings.rate-limit.mail-test-per-hour:20}") int mailTestLimit,
            @Value("${app.settings.rate-limit.secret-rotation-per-day:10}") int secretRotationLimit,
            @Value("${app.settings.rate-limit.count-failed-mail-tests:false}") boolean countFailedMailTests
    ) {
        this.mailTestLimit = mailTestLimit;
        this.mailTestWindow = Duration.ofHours(1);
        this.secretRotationLimit = secretRotationLimit;
        this.secretRotationWindow = Duration.ofDays(1);
        this.countFailedMailTests = countFailedMailTests;
    }

    public void checkMailTest(String userKey) {
        enforceLimit(
                mailTests,
                userKey,
                mailTestLimit,
                mailTestWindow,
                "mail test",
                "hour"
        );
    }

    public void recordMailTest(String userKey, boolean success) {
        if (!success && !countFailedMailTests) {
            return;
        }
        recordAttempt(mailTests, userKey);
    }

    public void checkSecretRotation(String userKey) {
        enforceLimit(
                secretRotations,
                userKey,
                secretRotationLimit,
                secretRotationWindow,
                "secret rotation",
                "day"
        );
    }

    public void recordSecretRotation(String userKey) {
        recordAttempt(secretRotations, userKey);
    }

    private void enforceLimit(
            Map<String, List<Instant>> buckets,
            String userKey,
            int limit,
            Duration window,
            String actionLabel,
            String windowLabel
    ) {
        Instant cutoff = Instant.now().minus(window);
        List<Instant> attempts = buckets.computeIfAbsent(userKey, ignored -> new ArrayList<>());
        synchronized (attempts) {
            attempts.removeIf(instant -> instant.isBefore(cutoff));
            if (attempts.size() >= limit) {
                OOSMLogger.warn(this.getClass(),
                        "Admin settings rate limit exceeded: user={} action={} limit={} window={}",
                        userKey, actionLabel, limit, windowLabel);
                throw new RateLimitExceededException(
                        "Rate limit exceeded: max " + limit + " " + actionLabel + "s per " + windowLabel);
            }
        }
    }

    private void recordAttempt(Map<String, List<Instant>> buckets, String userKey) {
        List<Instant> attempts = buckets.computeIfAbsent(userKey, ignored -> new ArrayList<>());
        synchronized (attempts) {
            attempts.add(Instant.now());
        }
    }

    public static class RateLimitExceededException extends RuntimeException {
        public RateLimitExceededException(String message) {
            super(message);
        }
    }
}