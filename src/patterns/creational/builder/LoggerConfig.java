package patterns.creational.builder;

/**
 * Immutable product: every field final, no setters, built only through the
 * Builder — an invalid config is unrepresentable because build() validates.
 */
public final class LoggerConfig {
    public enum Level { DEBUG, INFO, WARN, ERROR }

    private final String name;          // required
    private final Level level;          // optional with defaults below
    private final boolean console;
    private final String filePath;
    private final int maxFileSizeMb;    // meaningful only with filePath
    private final boolean jsonFormat;

    private LoggerConfig(Builder b) {
        this.name = b.name;
        this.level = b.level;
        this.console = b.console;
        this.filePath = b.filePath;
        this.maxFileSizeMb = b.maxFileSizeMb;
        this.jsonFormat = b.jsonFormat;
    }

    public static Builder named(String name) {
        return new Builder(name);
    }

    public static final class Builder {
        private final String name;
        private Level level = Level.INFO;
        private boolean console = true;
        private String filePath;
        private int maxFileSizeMb;
        private boolean jsonFormat;

        private Builder(String name) {
            this.name = name;
        }

        public Builder level(Level level) {
            this.level = level;
            return this;
        }

        public Builder console(boolean console) {
            this.console = console;
            return this;
        }

        public Builder file(String path, int maxFileSizeMb) {
            this.filePath = path;
            this.maxFileSizeMb = maxFileSizeMb;
            return this;
        }

        public Builder json() {
            this.jsonFormat = true;
            return this;
        }

        /** All validation in one place — including cross-field rules. */
        public LoggerConfig build() {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("logger name is required");
            }
            if (filePath != null && maxFileSizeMb <= 0) {
                throw new IllegalArgumentException("file sink needs maxFileSizeMb > 0, got " + maxFileSizeMb);
            }
            if (!console && filePath == null) {
                throw new IllegalArgumentException("at least one sink (console or file) is required");
            }
            return new LoggerConfig(this);
        }
    }

    @Override
    public String toString() {
        return "LoggerConfig{" + name + ", " + level
                + (console ? ", console" : "")
                + (filePath != null ? ", file=" + filePath + " (" + maxFileSizeMb + "MB)" : "")
                + (jsonFormat ? ", json" : "") + "}";
    }
}
