package problems.p07_loggingservice;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Demo {
    /** Blocks the worker on its first write until released — makes queue overflow deterministic. */
    static final class GateSink implements Sink {
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        private boolean first = true;

        @Override
        public String name() {
            return "gate";
        }

        @Override
        public LogLevel minLevel() {
            return LogLevel.DEBUG;
        }

        @Override
        public void write(LogRecord record) {
            if (first) {
                first = false;
                entered.countDown();
                try {
                    release.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Clock clock = Clock.fixed(Instant.parse("2026-10-01T12:00:00Z"), ZoneOffset.UTC);

        // --- functional demo: per-sink levels, decorators, rotation, failing sink ---
        RotatingFileSink file = new RotatingFileSink(LogLevel.DEBUG, new JsonFormatter(), 200);
        Sink failingRemote = new Sink() {
            @Override
            public String name() {
                return "remote";
            }

            @Override
            public LogLevel minLevel() {
                return LogLevel.ERROR;
            }

            @Override
            public void write(LogRecord record) {
                throw new IllegalStateException("remote endpoint down");
            }
        };
        AsyncLogger logger = new AsyncLogger(
                List.of(new ConsoleSink(LogLevel.INFO,
                                new TimestampDecorator(new ThreadNameDecorator(new SimpleFormatter()))),
                        file, failingRemote),
                LogLevel.DEBUG, 16, RejectionPolicy.BLOCK, clock);

        logger.debug("cache warmed");                 // console filters it out; file keeps it
        logger.info("service started");
        logger.error("db connection lost");           // remote sink throws -> isolated
        for (int i = 1; i <= 4; i++) {
            logger.info("request " + i + " served");  // pushes file sink past 200B -> rotation
        }
        logger.shutdown();                            // stop -> drain -> flush
        System.out.println("file sink total lines: " + file.totalLines());
        try {
            logger.info("after shutdown");
        } catch (IllegalStateException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // --- backpressure demo: bounded queue + DROP_NEWEST, counted honestly ---
        GateSink gate = new GateSink();
        AsyncLogger droppy = new AsyncLogger(List.of(gate), LogLevel.DEBUG, 2,
                RejectionPolicy.DROP_NEWEST, clock);
        droppy.info("r0");             // worker takes it and blocks inside the sink
        gate.entered.await();
        droppy.info("r1");             // queue slot 1
        droppy.info("r2");             // queue slot 2 — full
        droppy.info("r3");             // dropped
        droppy.info("r4");             // dropped
        gate.release.countDown();
        droppy.shutdown();
        System.out.println("dropped under full queue: " + droppy.droppedCount() + " (policy DROP_NEWEST)");
    }
}
