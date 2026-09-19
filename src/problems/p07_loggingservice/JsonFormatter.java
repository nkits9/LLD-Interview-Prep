package problems.p07_loggingservice;

public class JsonFormatter implements LogFormatter {
    @Override
    public String format(LogRecord record) {
        return "{\"level\":\"" + record.level() + "\",\"msg\":\"" + record.message()
                + "\",\"thread\":\"" + record.threadName() + "\",\"ts\":\"" + record.timestamp() + "\"}";
    }
}
