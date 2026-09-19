package problems.p07_loggingservice;

import java.util.ArrayList;
import java.util.List;

/**
 * Size-based rotation, simulated in memory so the demo is deterministic —
 * a real FileSink is this class with a Writer and a rename on rotate.
 * Only the single worker thread touches it: thread confinement, no locks.
 */
public class RotatingFileSink implements Sink {
    private final LogLevel minLevel;
    private final LogFormatter formatter;
    private final int maxBytesPerFile;

    private final List<List<String>> rotatedFiles = new ArrayList<>();
    private List<String> currentFile = new ArrayList<>();
    private int currentBytes;

    public RotatingFileSink(LogLevel minLevel, LogFormatter formatter, int maxBytesPerFile) {
        this.minLevel = minLevel;
        this.formatter = formatter;
        this.maxBytesPerFile = maxBytesPerFile;
    }

    @Override
    public String name() {
        return "file";
    }

    @Override
    public LogLevel minLevel() {
        return minLevel;
    }

    @Override
    public void write(LogRecord record) {
        String line = formatter.format(record);
        if (currentBytes + line.length() > maxBytesPerFile && !currentFile.isEmpty()) {
            rotate();
        }
        currentFile.add(line);
        currentBytes += line.length();
    }

    private void rotate() {
        rotatedFiles.add(currentFile);
        currentFile = new ArrayList<>();
        currentBytes = 0;
        System.out.println("[file] rotated -> app.log." + rotatedFiles.size());
    }

    @Override
    public void flush() {
        System.out.println("[file] flushed " + currentFile.size() + " lines (+"
                + rotatedFiles.size() + " rotated files)");
    }

    public int totalLines() {
        return currentFile.size() + rotatedFiles.stream().mapToInt(List::size).sum();
    }
}
