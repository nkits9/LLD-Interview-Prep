package problems.extras.notificationservice;

import java.time.Duration;

/** Injected like a Clock: real Thread.sleep in prod, recorded no-op in tests/demo. */
@FunctionalInterface
public interface Sleeper {
    void sleep(Duration duration) throws InterruptedException;
}
