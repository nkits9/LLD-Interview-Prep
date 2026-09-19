package problems.p08_ratelimiter;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * The strongest interview answer: per-client bucket, LAZY refill on access
 * (no timer per key), consume via a CAS loop on an immutable state — atomic
 * check-and-decrement with zero locks. Tokens in milli-units so fractional
 * refill needs no floating point.
 */
public class TokenBucketLimiter implements RateLimiter {
    private static final class Bucket {
        final long milliTokens;
        final long lastRefillMs;

        Bucket(long milliTokens, long lastRefillMs) {
            this.milliTokens = milliTokens;
            this.lastRefillMs = lastRefillMs;
        }
    }

    private final Function<String, Limits> limitsFor; // tiering: limits are a per-client lookup
    private final Clock clock;                        // injected: refill is testable
    private final Map<String, AtomicReference<Bucket>> buckets = new ConcurrentHashMap<>();

    public TokenBucketLimiter(Function<String, Limits> limitsFor, Clock clock) {
        this.limitsFor = limitsFor;
        this.clock = clock;
    }

    @Override
    public Decision check(String clientId) {
        Limits limits = limitsFor.apply(clientId);
        long capacityMilli = limits.capacity() * 1000L;
        long windowMs = limits.window().toMillis();
        long now = clock.millis();
        AtomicReference<Bucket> ref = buckets.computeIfAbsent(clientId,
                c -> new AtomicReference<>(new Bucket(capacityMilli, now))); // starts full: burst allowed

        while (true) {                                 // the CAS loop — write it from memory
            Bucket current = ref.get();
            long refilled = Math.min(capacityMilli,
                    current.milliTokens + (now - current.lastRefillMs) * capacityMilli / windowMs);
            if (refilled < 1000) {
                long deficit = 1000 - refilled;
                long retryAfter = (deficit * windowMs + capacityMilli - 1) / capacityMilli; // ceil
                return Decision.deny(retryAfter);
            }
            if (ref.compareAndSet(current, new Bucket(refilled - 1000, now))) {
                return Decision.allow();
            }
            // CAS lost: another thread consumed concurrently — recompute and retry
        }
    }

    @Override
    public int cleanupIdle(Duration idle) {
        long cutoff = clock.millis() - idle.toMillis();
        int before = buckets.size();
        buckets.entrySet().removeIf(e -> e.getValue().get().lastRefillMs < cutoff);
        return before - buckets.size();
    }
}
