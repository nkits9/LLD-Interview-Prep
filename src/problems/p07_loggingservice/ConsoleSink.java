package problems.p07_loggingservice;

public class ConsoleSink implements Sink {
    private final LogLevel minLevel;
    private final LogFormatter formatter;

    public ConsoleSink(LogLevel minLevel, LogFormatter formatter) {
        this.minLevel = minLevel;
        this.formatter = formatter;
    }

    @Override
    public String name() {
        return "console";
    }

    @Override
    public LogLevel minLevel() {
        return minLevel;
    }

    @Override
    public void write(LogRecord record) {
        System.out.println("[console] " + formatter.format(record));
    }
}
