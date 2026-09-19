package problems.p08_ratelimiter;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

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
        // Tiering is a lookup, not a different limiter.
        Function<String, Limits> tiers = client -> client.startsWith("pro:")
                ? new Limits(5, Duration.ofSeconds(1))
                : new Limits(3, Duration.ofSeconds(1));

        // --- token bucket: burst to capacity, lazy refill, retry-after ---
        SteppingClock clock = new SteppingClock();
        RateLimiter bucket = RateLimiterFactory.create(LimiterType.TOKEN_BUCKET, tiers, clock);
        for (int i = 1; i <= 4; i++) {
            System.out.println("bucket free:c1 req" + i + " -> " + bucket.check("free:c1"));
        }
        clock.advance(Duration.ofMillis(400)); // 3 tokens/s * 0.4s = 1.2 tokens refilled lazily
        System.out.println("after 400ms       -> " + bucket.check("free:c1"));
        System.out.println("and again         -> " + bucket.check("free:c1"));
        System.out.println("pro tier burst of 5: last -> "
                + burst(bucket, "pro:c9", 5) + " (higher tier, same limiter)");

        // --- fixed window: the boundary-burst weakness, shown live ---
        SteppingClock fwClock = new SteppingClock();
        RateLimiter fixed = RateLimiterFactory.create(LimiterType.FIXED_WINDOW, tiers, fwClock);
        fwClock.advance(Duration.ofMillis(900));
        int b1 = countAllowed(fixed, "free:c2", 3);
        fwClock.advance(Duration.ofMillis(150)); // crosses the window boundary
        int b2 = countAllowed(fixed, "free:c2", 3);
        System.out.println("fixed window: " + (b1 + b2) + " allowed in 150ms (cap 3/s) <- boundary burst");

        // --- sliding window counter: same scenario, smoothed ---
        SteppingClock swClock = new SteppingClock();
        RateLimiter sliding = RateLimiterFactory.create(LimiterType.SLIDING_WINDOW_COUNTER, tiers, swClock);
        swClock.advance(Duration.ofMillis(900));
        int s1 = countAllowed(sliding, "free:c3", 3);
        swClock.advance(Duration.ofMillis(150));
        Decision d = sliding.check("free:c3");
        System.out.println("sliding window: " + s1 + " then " + d + " <- burst smoothed");

        // --- CAS correctness: 20 threads race one bucket of 5 — EXACTLY 5 pass ---
        RateLimiter raceBucket = RateLimiterFactory.create(LimiterType.TOKEN_BUCKET, tiers, clock);
        ExecutorService pool = Executors.newFixedThreadPool(20);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger allowed = new AtomicInteger();
        for (int i = 0; i < 20; i++) {
            pool.submit(() -> {
                start.await();
                if (raceBucket.check("pro:race").allowed()) {
                    allowed.incrementAndGet();
                }
                return null;
            });
        }
        start.countDown();
        pool.shutdown();
        if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("race demo did not finish");
        }
        System.out.println("race: 20 threads, capacity 5 -> allowed = " + allowed.get());

        // --- idle cleanup: stale client state evicted, no timer per key ---
        clock.advance(Duration.ofHours(2));
        System.out.println("idle cleanup evicted: " + bucket.cleanupIdle(Duration.ofHours(1)) + " clients");
    }

    private static Decision burst(RateLimiter limiter, String client, int n) {
        Decision last = null;
        for (int i = 0; i < n; i++) {
            last = limiter.check(client);
        }
        return last;
    }

    private static int countAllowed(RateLimiter limiter, String client, int n) {
        int allowed = 0;
        for (int i = 0; i < n; i++) {
            if (limiter.check(client).allowed()) {
                allowed++;
            }
        }
        return allowed;
    }
}
