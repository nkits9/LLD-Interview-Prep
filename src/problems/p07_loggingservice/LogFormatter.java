package problems.p07_loggingservice;

/** Formatting varies per sink (plain, JSON) — Strategy; decorators add fields. */
@FunctionalInterface
public interface LogFormatter {
    String format(LogRecord record);
}
