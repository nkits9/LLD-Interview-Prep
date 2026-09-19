package problems.p07_loggingservice;

/** Ordered — filtering is a comparison, not a chain of ifs. */
public enum LogLevel {
    DEBUG, INFO, WARN, ERROR;

    public boolean atLeast(LogLevel min) {
        return ordinal() >= min.ordinal();
    }
}
