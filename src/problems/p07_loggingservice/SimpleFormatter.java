package problems.p07_loggingservice;

public class SimpleFormatter implements LogFormatter {
    @Override
    public String format(LogRecord record) {
        return record.level() + " " + record.message();
    }
}
