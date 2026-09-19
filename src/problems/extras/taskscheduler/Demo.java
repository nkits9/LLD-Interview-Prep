package problems.extras.taskscheduler;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Demo {
    static final class SteppingClock extends Clock {
        private Instant now = Instant.parse("2026-10-01T09:00:00Z");

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

    private static void tickAndJoin(Scheduler scheduler) {
        for (Future<?> f : scheduler.tick()) {
            try {
                f.get(5, TimeUnit.SECONDS);
            } catch (ExecutionException | InterruptedException | java.util.concurrent.TimeoutException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        SteppingClock clock = new SteppingClock();
        ExecutorService pool = Executors.newFixedThreadPool(4);
        Scheduler scheduler = new Scheduler(pool, clock, Duration.ofSeconds(60));

        // One-shot task + idempotent duplicate submission.
        scheduler.schedule("daily-report", "report", Duration.ofSeconds(10), 0,
                () -> System.out.println("[task] report generated"));
        boolean duplicate = scheduler.schedule("daily-report", "report", Duration.ofSeconds(10), 0,
                () -> System.out.println("[task] report generated AGAIN?!"));
        System.out.println("duplicate submission accepted: " + duplicate);

        // Recurring heartbeat every 30s.
        AtomicInteger beats = new AtomicInteger();
        scheduler.scheduleRecurring("hb", "heartbeat", Duration.ofSeconds(30),
                () -> System.out.println("[task] heartbeat #" + beats.incrementAndGet()));

        // Failing task: 2 retries with exponential backoff, then dead-letter.
        scheduler.schedule("upload", "flaky-upload", Duration.ofSeconds(5), 2,
                () -> {
                    throw new IllegalStateException("remote store down");
                });

        clock.advance(Duration.ofSeconds(10)); // upload(5s) + report(10s) due; heartbeat(30s) not
        tickAndJoin(scheduler);
        clock.advance(Duration.ofSeconds(60)); // retry#1 (60s backoff) + heartbeat due
        tickAndJoin(scheduler);
        clock.advance(Duration.ofSeconds(120)); // retry#2 (120s backoff) + heartbeat again
        tickAndJoin(scheduler);
        clock.advance(Duration.ofSeconds(120)); // retry #3 would be here → exhausted instead
        tickAndJoin(scheduler);

        System.out.println("dead letter: " + scheduler.deadLetter());
        System.out.println("pending (heartbeat rearmed): " + scheduler.pendingCount());

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
    }
}
