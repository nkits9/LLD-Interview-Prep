package problems.extras.connectionpool;

import java.time.Duration;

/** Fail fast after the timeout — callers back off; nobody waits forever. */
public class PoolExhaustedException extends RuntimeException {
    public PoolExhaustedException(Duration waited) {
        super("no connection free within " + waited.toMillis() + "ms");
    }
}
