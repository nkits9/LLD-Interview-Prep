package problems.extras.connectionpool;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The wrapper callers hold: close() RETURNS the underlying connection to the
 * pool instead of destroying it (the whole trick), and use-after-return fails
 * loudly. Double-close is safe (CAS guard).
 */
public class LeasedConnection implements Connection {
    private final Connection delegate;
    private final ConnectionPool pool;
    private final AtomicBoolean returned = new AtomicBoolean(false);

    LeasedConnection(Connection delegate, ConnectionPool pool) {
        this.delegate = delegate;
        this.pool = pool;
    }

    @Override
    public String id() {
        return delegate.id();
    }

    @Override
    public boolean isHealthy() {
        return delegate.isHealthy();
    }

    @Override
    public String query(String sql) {
        if (returned.get()) {
            throw new IllegalStateException("connection already returned to pool");
        }
        return delegate.query(sql);
    }

    @Override
    public void close() {
        if (returned.compareAndSet(false, true)) {
            pool.release(delegate);
        }
    }
}
