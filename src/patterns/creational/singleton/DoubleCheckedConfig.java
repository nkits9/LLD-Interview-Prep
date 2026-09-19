package patterns.creational.singleton;

/**
 * Double-checked locking — the form interviewers grill on.
 * volatile is NOT optional: without it, a thread can observe a non-null
 * reference to a partially constructed instance (no happens-before).
 */
public final class DoubleCheckedConfig {

    private static volatile DoubleCheckedConfig instance;

    private DoubleCheckedConfig() {
    }

    public static DoubleCheckedConfig getInstance() {
        DoubleCheckedConfig local = instance;          // single volatile read on the fast path
        if (local == null) {                           // first check: skip the lock when built
            synchronized (DoubleCheckedConfig.class) {
                local = instance;
                if (local == null) {                   // second check: another thread may have won
                    instance = local = new DoubleCheckedConfig();
                }
            }
        }
        return local;
    }
}
