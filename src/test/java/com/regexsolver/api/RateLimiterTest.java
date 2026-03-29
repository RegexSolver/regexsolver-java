package com.regexsolver.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RateLimiterTest {

    @Test
    void testRateLimiterWaitIfNecessaryNoWait() {
        RateLimiter rl = RateLimiter.getInstance("test-token-1");

        long start = System.currentTimeMillis();
        // Should complete immediately since we haven't triggered a wait
        rl.waitIfNecessary().join();
        long end = System.currentTimeMillis();

        assertThat(end - start).isLessThan(50); // Allowing a small buffer for execution time
    }

    @Test
    void testRateLimiterTriggerAndWait() {
        RateLimiter rl = RateLimiter.getInstance("test-token-2");

        // Trigger a 0.2 second (200ms) delay
        rl.trigger(0.2);

        long start = System.currentTimeMillis();
        rl.waitIfNecessary().join();
        long end = System.currentTimeMillis();

        long duration = end - start;
        // It should have waited at least ~200ms
        assertThat(duration).isGreaterThanOrEqualTo(150); // 150ms to prevent flaky tests on slow CI
    }

    @Test
    void testRateLimiterTriggerAlreadyCleared() {
        RateLimiter rl = RateLimiter.getInstance("test-token-3");

        // Trigger a 0.1 second delay
        rl.trigger(0.1);

        // Immediately trigger a shorter delay (0.05 seconds)
        rl.trigger(0.05);

        long start = System.currentTimeMillis();
        rl.waitIfNecessary().join();
        long end = System.currentTimeMillis();

        long duration = end - start;

        // The rate limiter should respect the LONGER of the two triggered delays
        // Next retry was set to now + 100ms, the second call tried to set it to now + 50ms,
        // but updateAndGet ensures it keeps the later Instant.
        assertThat(duration).isGreaterThanOrEqualTo(80);
    }

    @Test
    void testGetInstanceReturnsSameInstanceForSameToken() {
        RateLimiter rl1 = RateLimiter.getInstance("tokenA");
        RateLimiter rl2 = RateLimiter.getInstance("tokenA");

        assertThat(rl1).isSameAs(rl2);
    }

    @Test
    void testGetInstanceReturnsDifferentInstanceForDifferentTokens() {
        RateLimiter rl1 = RateLimiter.getInstance("tokenB");
        RateLimiter rl2 = RateLimiter.getInstance("tokenC");

        assertThat(rl1).isNotSameAs(rl2);
    }
}
