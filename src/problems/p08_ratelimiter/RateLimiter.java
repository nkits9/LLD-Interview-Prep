package problems.p08_ratelimiter;

import java.time.Duration;

/** The algorithm varies (token bucket, windows, leaky bucket) — Strategy. */
public interface RateLimiter {
    Decision check(String clientId);

    /** Evict per-client state idle longer than this; returns entries removed. */
    default int cleanupIdle(Duration idle) {
        return 0;
    }
}
