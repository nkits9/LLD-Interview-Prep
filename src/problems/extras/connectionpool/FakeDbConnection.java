package problems.extras.connectionpool;

import java.util.concurrent.atomic.AtomicBoolean;

/** Stands in for a real socket-backed connection; health is togglable for demos. */
public class FakeDbConnection implements Connection {
    private final String id;
    private final AtomicBoolean healthy = new AtomicBoolean(true);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    FakeDbConnection(String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public boolean isHealthy() {
        return healthy.get() && !closed.get();
    }

    public void breakConnection() {
        healthy.set(false);
    }

    @Override
    public String query(String sql) {
        if (closed.get()) {
            throw new IllegalStateException(id + " is closed");
        }
        return id + " -> rows for [" + sql + "]";
    }

    @Override
    public void close() {
        closed.set(true);
    }
}
