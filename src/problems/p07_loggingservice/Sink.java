package problems.p07_loggingservice;

/**
 * A destination with its OWN min level and formatter — per-sink filtering.
 * The dispatcher isolates a failing sink so the others keep logging.
 */
public interface Sink {
    String name();

    LogLevel minLevel();

    void write(LogRecord record);

    default void flush() {
    }
}
