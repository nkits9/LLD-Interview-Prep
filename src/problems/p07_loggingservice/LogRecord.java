package problems.p07_loggingservice;

import java.time.Instant;

/** Immutable event captured at the call site — safe to hand across threads. */
public final class LogRecord {
    private final LogLevel level;
    private final String message;
    private final String threadName;
    private final Instant timestamp;

    LogRecord(LogLevel level, String message, String threadName, Instant timestamp) {
        this.level = level;
        this.message = message;
        this.threadName = threadName;
        this.timestamp = timestamp;
    }

    public LogLevel level() {
        return level;
    }

    public String message() {
        return message;
    }

    public String threadName() {
        return threadName;
    }

    public Instant timestamp() {
        return timestamp;
    }
}
