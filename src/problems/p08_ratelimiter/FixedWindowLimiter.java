package problems.p08_ratelimiter;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Simplest algorithm — and it has the KNOWN boundary weakness: a client can
 * burst 2x capacity straddling the window edge. Implement it, then say why
 * sliding-window exists.
 */
public class FixedWindowLimiter implements RateLimiter {
    private static final class Window {
        final long startMs;
        final int count;

        Window(long startMs, int count) {
            this.startMs = startMs;
            this.count = count;
        }
    }

    private final Function<String, Limits> limitsFor;
    private final Clock clock;
    private final Map<String, AtomicReference<Window>> windows = new ConcurrentHashMap<>();

    public FixedWindowLimiter(Function<String, Limits> limitsFor, Clock clock) {
        this.limitsFor = limitsFor;
        this.clock = clock;
    }

    @Override
    public Decision check(String clientId) {
        Limits limits = limitsFor.apply(clientId);
        long windowMs = limits.window().toMillis();
        long now = clock.millis();
        long windowStart = (now / windowMs) * windowMs;   // aligned windows
        AtomicReference<Window> ref = windows.computeIfAbsent(clientId,
                c -> new AtomicReference<>(new Window(windowStart, 0)));

        while (true) {
            Window current = ref.get();
            Window next;
            if (current.startMs != windowStart) {
                next = new Window(windowStart, 1);        // fresh window
            } else if (current.count < limits.capacity()) {
                next = new Window(windowStart, current.count + 1);
            } else {
                return Decision.deny(windowStart + windowMs - now);
            }
            if (ref.compareAndSet(current, next)) {
                return Decision.allow();
            }
        }
    }
}
