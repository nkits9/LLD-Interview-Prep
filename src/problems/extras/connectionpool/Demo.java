package problems.extras.connectionpool;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Demo {
    public static void main(String[] args) throws InterruptedException {
        AtomicInteger created = new AtomicInteger();
        java.util.List<FakeDbConnection> allCreated =
                java.util.Collections.synchronizedList(new java.util.ArrayList<>());
        ConnectionPool pool = new ConnectionPool(2, () -> {
            FakeDbConnection c = new FakeDbConnection("conn-" + created.incrementAndGet());
            allCreated.add(c);
            return c;
        });

        // Lazy creation up to max; the 3rd acquire times out fast (bounded wait).
        Connection c1 = pool.acquire(Duration.ofMillis(100));
        Connection c2 = pool.acquire(Duration.ofMillis(100));
        System.out.println("leased: " + c1.id() + ", " + c2.id());
        try {
            pool.acquire(Duration.ofMillis(100));
        } catch (PoolExhaustedException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // Return → reuse the SAME underlying connection (that's the point of pooling).
        c1.close();
        Connection c3 = pool.acquire(Duration.ofMillis(100));
        System.out.println("reused  : " + c3.id() + " (no new connection created)");

        // Use-after-return fails loudly; double-close is safe.
        try {
            c1.query("select 1");
        } catch (IllegalStateException e) {
            System.out.println("rejected: " + e.getMessage());
        }
        c1.close(); // no-op, not a corruption

        // Health check on release: a broken connection is destroyed, not recycled.
        allCreated.get(1).breakConnection(); // conn-2, currently leased as c2
        c2.close();
        Connection c4 = pool.acquire(Duration.ofMillis(100));
        System.out.println("replaced: " + c4.id() + " (sick conn discarded; total created = "
                + created.get() + ")");

        // Return everything before the stress — leaked leases starve the pool (that IS the classic bug).
        c3.close();
        c4.close();

        // Stress: 8 workers, pool of 2 — everything completes, creations stay bounded.
        ExecutorService workers = Executors.newFixedThreadPool(8);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger queries = new AtomicInteger();
        for (int i = 0; i < 8; i++) {
            workers.submit(() -> {
                start.await();
                for (int j = 0; j < 50; j++) {
                    Connection c = pool.acquire(Duration.ofSeconds(2));
                    try {
                        c.query("select " + j);
                        queries.incrementAndGet();
                    } finally {
                        c.close();
                    }
                }
                return null;
            });
        }
        start.countDown();
        workers.shutdown();
        if (!workers.awaitTermination(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("stress did not finish");
        }
        System.out.println("stress  : " + queries.get() + " queries via max 2 connections; created = "
                + created.get());

        pool.shutdown();
        try {
            pool.acquire(Duration.ofMillis(50));
        } catch (IllegalStateException e) {
            System.out.println("rejected: " + e.getMessage());
        }
    }

}
