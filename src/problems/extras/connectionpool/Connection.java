package problems.extras.connectionpool;

/** What callers use; the pool hands out leased wrappers of the real thing. */
public interface Connection {
    String id();

    boolean isHealthy();

    String query(String sql);

    void close();
}
