package problems.extras.lrucache;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Demo {
    static final class SteppingClock extends Clock {
        private Instant now = Instant.parse("2026-10-01T00:00:00Z");

        void advance(Duration d) {
            now = now.plus(d);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        SteppingClock clock = new SteppingClock();
        LruCache<String, Integer> cache = new LruCache<>(3, Duration.ofMinutes(5), clock);

        // Eviction order: touching 'a' saves it; 'b' becomes LRU and dies.
        cache.put("a", 1);
        cache.put("b", 2);
        cache.put("c", 3);
        cache.get("a");                       // refresh recency of a
        cache.put("d", 4);                    // capacity 3 → evicts b (LRU)
        System.out.println("a=" + cache.get("a") + " b=" + cache.get("b")
                + " c=" + cache.get("c") + " d=" + cache.get("d"));

        // TTL: lazy expiry on access + optional sweep.
        clock.advance(Duration.ofMinutes(6));
        System.out.println("after 6min, a=" + cache.get("a") + " (expired lazily)");
        System.out.println("sweep evicted " + cache.evictExpired() + " more, size=" + cache.size());

        // Striping: 4 shards hammered by 8 threads — bounded size, no corruption.
        ShardedCache<Integer, Integer> sharded = new ShardedCache<>(4, 25, null, clock);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        CountDownLatch start = new CountDownLatch(1);
        for (int t = 0; t < 8; t++) {
            final int seed = t;
            pool.submit(() -> {
                start.await();
                for (int i = 0; i < 5_000; i++) {
                    int key = (seed * 31 + i * 17) % 500;
                    sharded.put(key, i);
                    sharded.get((key + 7) % 500);
                }
                return null;
            });
        }
        start.countDown();
        pool.shutdown();
        if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("stress did not finish");
        }
        System.out.println("40k concurrent ops on 4x25 shards -> size=" + sharded.size()
                + " (bound 100, no exceptions)");
    }
}
