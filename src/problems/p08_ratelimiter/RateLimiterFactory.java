package problems.p08_ratelimiter;

import java.time.Clock;
import java.util.function.Function;

/** Strategy + Factory pairing: config names the algorithm, one place constructs it. */
public final class RateLimiterFactory {
    private RateLimiterFactory() {
    }

    public static RateLimiter create(LimiterType type, Function<String, Limits> limitsFor, Clock clock) {
        switch (type) { // the if-else exists — in exactly one place
            case TOKEN_BUCKET:
                return new TokenBucketLimiter(limitsFor, clock);
            case FIXED_WINDOW:
                return new FixedWindowLimiter(limitsFor, clock);
            case SLIDING_WINDOW_COUNTER:
                return new SlidingWindowCounterLimiter(limitsFor, clock);
            default:
                throw new IllegalArgumentException("unsupported limiter type " + type);
        }
    }
}
