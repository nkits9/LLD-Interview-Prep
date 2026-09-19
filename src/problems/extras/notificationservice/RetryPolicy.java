package problems.extras.notificationservice;

import java.time.Duration;

/** Exponential backoff: base * 2^(attempt-1). Production adds jitter — say it. */
public final class RetryPolicy {
    private final int maxAttempts;
    private final Duration baseDelay;

    public RetryPolicy(int maxAttempts, Duration baseDelay) {
        this.maxAttempts = maxAttempts;
        this.baseDelay = baseDelay;
    }

    public int maxAttempts() {
        return maxAttempts;
    }

    public Duration delayBeforeAttempt(int attempt) {
        return baseDelay.multipliedBy(1L << (attempt - 2)); // before attempt 2: base, then 2x, 4x…
    }
}
