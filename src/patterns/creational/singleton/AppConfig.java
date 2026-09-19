package patterns.creational.singleton;

/**
 * Holder idiom — the form to reach for: lazy, thread-safe, zero locking.
 * The JVM guarantees Holder is initialized exactly once, on first access.
 */
public final class AppConfig {

    private AppConfig() {
        // private: no outside instantiation
    }

    private static final class Holder {
        private static final AppConfig INSTANCE = new AppConfig();
    }

    public static AppConfig getInstance() {
        return Holder.INSTANCE;
    }

    public String get(String key) {
        return "value-of-" + key;
    }
}
