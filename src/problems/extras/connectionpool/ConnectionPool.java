package problems.extras.connectionpool;

import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Semaphore = "how many connections may be OUT" (the bound); idle queue = the
 * warm ones; creation is lazy up to max. Release health-checks: sick
 * connections are destroyed, and the permit freed either way — permits must
 * never leak.
 */
public class ConnectionPool {
    private final Supplier<Connection> factory;
    private final Semaphore permits;
    private final Queue<Connection> idle = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean open = new AtomicBoolean(true);

    public ConnectionPool(int maxSize, Supplier<Connection> factory) {
        this.factory = factory;
        this.permits = new Semaphore(maxSize, true); // fair: waiters served FIFO
    }

    /** Blocking acquire WITH TIMEOUT — user-facing paths never wait forever. */
    public Connection acquire(Duration timeout) throws InterruptedException {
        if (!open.get()) {
            throw new IllegalStateException("pool is shut down");
        }
        if (!permits.tryAcquire(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
            throw new PoolExhaustedException(timeout);
        }
        try {
            Connection connection = idle.poll();
            if (connection == null) {
                connection = factory.get();             // lazy create, bounded by the semaphore
            }
            return new LeasedConnection(connection, this);
        } catch (RuntimeException e) {
            permits.release();                          // creation failed → permit must not leak
            throw e;
        }
    }

    /** Health check at return: recycle the good, destroy the sick — permit freed either way. */
    void release(Connection connection) {
        try {
            if (open.get() && connection.isHealthy()) {
                idle.offer(connection);
            } else {
                connection.close();                     // discard; next acquire creates a fresh one
            }
        } finally {
            permits.release();
        }
    }

    public void shutdown() {
        open.set(false);
        Connection connection;
        while ((connection = idle.poll()) != null) {
            connection.close();
        }
    }

    public int availablePermits() {
        return permits.availablePermits();
    }
}
