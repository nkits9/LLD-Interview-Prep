package problems.p07_loggingservice;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Producer-consumer: callers enqueue, ONE worker thread drains to sinks —
 * sinks are thread-confined (no locks) and slow sinks never block callers.
 * "Thread-safe global access without a hard Singleton": create one at startup
 * and inject it; nothing here is static.
 */
public class AsyncLogger {
    private static final LogRecord SHUTDOWN_SENTINEL =
            new LogRecord(LogLevel.DEBUG, "<shutdown>", "-", Instant.EPOCH);

    private final BlockingQueue<LogRecord> queue;
    private final List<Sink> sinks;
    private final LogLevel minLevel;
    private final RejectionPolicy policy;
    private final Clock clock;
    private final Thread worker;
    private final AtomicLong dropped = new AtomicLong();
    private volatile boolean accepting = true;

    public AsyncLogger(List<Sink> sinks, LogLevel minLevel, int queueCapacity,
                       RejectionPolicy policy, Clock clock) {
        this.queue = new ArrayBlockingQueue<>(queueCapacity); // bounded, ALWAYS
        this.sinks = List.copyOf(sinks);
        this.minLevel = minLevel;
        this.policy = policy;
        this.clock = clock;
        this.worker = new Thread(this::drainLoop, "log-worker");
        this.worker.start();
    }

    public void log(LogLevel level, String message) {
        if (!accepting) {
            throw new IllegalStateException("logger is shut down");
        }
        if (!level.atLeast(minLevel)) {
            return;
        }
        LogRecord record = new LogRecord(level, message,
                Thread.currentThread().getName(), clock.instant());
        try {
            if (policy == RejectionPolicy.BLOCK) {
                queue.put(record);                          // backpressure
            } else if (!queue.offer(record)) {
                dropped.incrementAndGet();                  // shed + count, never lie silently
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void info(String msg) {
        log(LogLevel.INFO, msg);
    }

    public void debug(String msg) {
        log(LogLevel.DEBUG, msg);
    }

    public void error(String msg) {
        log(LogLevel.ERROR, msg);
    }

    private void drainLoop() {
        try {
            while (true) {
                LogRecord record = queue.take();
                if (record == SHUTDOWN_SENTINEL) {
                    return;
                }
                dispatch(record);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void dispatch(LogRecord record) {
        for (Sink sink : sinks) {
            if (!record.level().atLeast(sink.minLevel())) {
                continue;                                    // per-sink filtering
            }
            try {
                sink.write(record);
            } catch (RuntimeException e) {
                // A failing sink must not break the others — isolate and move on.
                System.out.println("[logger] sink '" + sink.name() + "' failed, continuing: "
                        + e.getMessage());
            }
        }
    }

    /** Graceful: stop accepting -> drain everything queued -> flush sinks -> join. */
    public void shutdown() throws InterruptedException {
        accepting = false;
        queue.put(SHUTDOWN_SENTINEL); // everything enqueued before this still gets written
        worker.join();
        sinks.forEach(Sink::flush);
    }

    public long droppedCount() {
        return dropped.get();
    }
}
