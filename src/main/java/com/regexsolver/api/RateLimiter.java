package com.regexsolver.api;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Global rate limiter shared by apiToken.
 *
 * Holds a single deadline. {@code trigger} keeps the later of the current and
 * the new deadline; {@code waitIfNecessary} schedules a non-blocking delay
 * (no thread is ever parked) and re-checks the deadline after every wake, so
 * a deadline extended by a concurrent 429 is honored.
 */
class RateLimiter {

    private static final ConcurrentHashMap<String, RateLimiter> INSTANCES =
        new ConcurrentHashMap<>();

    private final AtomicReference<Instant> retryAfter = new AtomicReference<>(
        Instant.MIN
    );

    private RateLimiter() {}

    public static RateLimiter getInstance(String apiToken) {
        return INSTANCES.computeIfAbsent(apiToken, k -> new RateLimiter());
    }

    public CompletableFuture<Void> waitIfNecessary() {
        Instant now = Instant.now();
        Instant retryAt = retryAfter.get();

        if (!retryAt.isAfter(now)) {
            return CompletableFuture.completedFuture(null);
        }
        long delay = Math.max(Duration.between(now, retryAt).toMillis(), 1);
        return CompletableFuture.runAsync(
            () -> {},
            CompletableFuture.delayedExecutor(delay, TimeUnit.MILLISECONDS)
        ).thenCompose(v -> waitIfNecessary());
    }

    public void trigger(double seconds) {
        Instant nextRetry = Instant.now().plus(
            Duration.ofMillis((long) (seconds * 1000))
        );
        retryAfter.updateAndGet(current ->
            nextRetry.isAfter(current) ? nextRetry : current
        );
    }
}
