package com.regexsolver.api;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Global rate limiter shared by apiToken.
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

        if (retryAt.isAfter(now)) {
            long delay = Duration.between(now, retryAt).toMillis();
            if (delay > 0) {
                return CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        }
        return CompletableFuture.completedFuture(null);
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
