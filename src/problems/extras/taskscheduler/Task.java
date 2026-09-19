package problems.extras.taskscheduler;

import java.time.Duration;
import java.time.Instant;

/** A unit of scheduled work. Mutable scheduling fields are guarded by the scheduler's lock. */
public class Task {
    final String key;         // idempotency: one active task per key
    final String name;
    final Runnable action;
    final Duration interval;  // null = one-shot, else fixed-rate recurring
    final int maxRetries;
    final long seq;           // FIFO tie-break for equal run times

    Instant nextRunAt;
    int attempts;

    Task(String key, String name, Runnable action, Instant nextRunAt,
         Duration interval, int maxRetries, long seq) {
        this.key = key;
        this.name = name;
        this.action = action;
        this.nextRunAt = nextRunAt;
        this.interval = interval;
        this.maxRetries = maxRetries;
        this.seq = seq;
    }

    @Override
    public String toString() {
        return name + "(key=" + key + ", attempts=" + attempts + ")";
    }
}
