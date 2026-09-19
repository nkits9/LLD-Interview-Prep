package problems.p08_ratelimiter;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Fixes the fixed-window boundary burst with two counters: the previous
 * window's count is weighted by how much of it still overlaps the sliding
 * window. O(1) memory per client (vs the exact-but-heavy sliding LOG of
 * timestamps).
 */
public class SlidingWindowCounterLimiter implements RateLimiter {
    private static final class State {
        final long windowStartMs;
        final int currentCount;
        final int previousCount;

        State(long windowStartMs, int currentCount, int previousCount) {
            this.windowStartMs = windowStartMs;
            this.currentCount = currentCount;
            this.previousCount = previousCount;
        }
    }

    private final Function<String, Limits> limitsFor;
    private final Clock clock;
    private final Map<String, AtomicReference<State>> states = new ConcurrentHashMap<>();

    public SlidingWindowCounterLimiter(Function<String, Limits> limitsFor, Clock clock) {
        this.limitsFor = limitsFor;
        this.clock = clock;
    }

    @Override
    public Decision check(String clientId) {
        Limits limits = limitsFor.apply(clientId);
        long windowMs = limits.window().toMillis();
        long now = clock.millis();
        long windowStart = (now / windowMs) * windowMs;
        AtomicReference<State> ref = states.computeIfAbsent(clientId,
                c -> new AtomicReference<>(new State(windowStart, 0, 0)));

        while (true) {
            State original = ref.get();                   // CAS expected value
            State current = rollForward(original, windowStart, windowMs);
            // weighted estimate in milli-units — integer floor must not re-open the boundary burst
            long elapsedInWindow = now - windowStart;
            long weightedMilli = current.currentCount * 1000L
                    + current.previousCount * 1000L * (windowMs - elapsedInWindow) / windowMs;
            if (weightedMilli + 1000 > limits.capacity() * 1000L) {
                long retryAfter = current.previousCount == 0 ? windowStart + windowMs - now
                        : estimateRetry(limits, current, windowMs, elapsedInWindow);
                return Decision.deny(Math.max(1, retryAfter));
            }
            State next = new State(current.windowStartMs, current.currentCount + 1,
                    current.previousCount);
            if (ref.compareAndSet(original, next)) {
                return Decision.allow();
            }
        }
    }

    /** Window moved on: current becomes previous (or everything ages out). */
    private State rollForward(State state, long windowStart, long windowMs) {
        if (state.windowStartMs == windowStart) {
            return state;
        }
        boolean adjacent = state.windowStartMs == windowStart - windowMs;
        return new State(windowStart, 0, adjacent ? state.currentCount : 0);
    }

    /** How long until the previous window's weight decays enough for one request. */
    private long estimateRetry(Limits limits, State s, long windowMs, long elapsedInWindow) {
        long room = limits.capacity() - 1 - s.currentCount;
        long neededElapsed = windowMs - (room * windowMs / s.previousCount);
        return Math.max(1, neededElapsed - elapsedInWindow);
    }
}
