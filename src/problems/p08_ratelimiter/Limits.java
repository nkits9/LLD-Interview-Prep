package problems.p08_ratelimiter;

import java.time.Duration;

/** capacity requests per window — resolved PER CLIENT (tiering is a lookup, not a limiter). */
public final class Limits {
    private final int capacity;
    private final Duration window;

    public Limits(int capacity, Duration window) {
        if (capacity < 1 || window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("capacity >= 1 and positive window required");
        }
        this.capacity = capacity;
        this.window = window;
    }

    public int capacity() {
        return capacity;
    }

    public Duration window() {
        return window;
    }
}
