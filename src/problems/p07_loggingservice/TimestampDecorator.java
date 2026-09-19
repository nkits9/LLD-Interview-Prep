package problems.p07_loggingservice;

/** Decorator: adds the timestamp around ANY formatter. */
public class TimestampDecorator implements LogFormatter {
    private final LogFormatter inner;

    public TimestampDecorator(LogFormatter inner) {
        this.inner = inner;
    }

    @Override
    public String format(LogRecord record) {
        return record.timestamp() + " " + inner.format(record);
    }
}
