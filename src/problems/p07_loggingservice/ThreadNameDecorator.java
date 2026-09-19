package problems.p07_loggingservice;

/** Decorator: adds the producing thread's name. */
public class ThreadNameDecorator implements LogFormatter {
    private final LogFormatter inner;

    public ThreadNameDecorator(LogFormatter inner) {
        this.inner = inner;
    }

    @Override
    public String format(LogRecord record) {
        return "[" + record.threadName() + "] " + inner.format(record);
    }
}
