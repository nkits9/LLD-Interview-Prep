package problems.p02_amazonlocker;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Demo {
    /** Injected clock we can advance — expiry is testable without waiting 3 days. */
    static final class SteppingClock extends Clock {
        private Instant now = Instant.parse("2026-10-01T10:00:00Z");

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
        AtomicInteger otpSeq = new AtomicInteger();
        LockerService service = new LockerService(
                List.of(new Locker("L-S1", Size.SMALL), new Locker("L-M1", Size.MEDIUM),
                        new Locker("L-L1", Size.LARGE)),
                new SmallestFitSelection(),
                () -> "OTP-" + otpSeq.incrementAndGet(),
                clock,
                Duration.ofDays(3));

        // Smallest fit: a SMALL package takes the SMALL locker, not the LARGE one.
        Assignment a1 = service.assign(new Package("PKG-1", Size.SMALL));
        System.out.println("PKG-1 assigned: " + a1);

        // Failure: OTP is single-use and validated.
        try {
            service.pickup(a1.lockerId(), "OTP-999");
        } catch (InvalidOtpException e) {
            System.out.println("rejected: " + e.getMessage());
        }
        System.out.println("PKG picked up : " + service.pickup(a1.lockerId(), a1.otpCode()));
        try {
            service.pickup(a1.lockerId(), a1.otpCode()); // locker now empty + code consumed
        } catch (InvalidOtpException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // Freed locker is immediately reusable (atomic return to pool).
        Assignment a2 = service.assign(new Package("PKG-2", Size.SMALL));
        System.out.println("PKG-2 assigned: " + a2 + "   <- same locker, recycled");

        // Failure: nothing fits when compatible lockers are exhausted.
        service.assign(new Package("PKG-3", Size.MEDIUM));
        service.assign(new Package("PKG-4", Size.LARGE));
        try {
            service.assign(new Package("PKG-5", Size.MEDIUM));
        } catch (NoLockerAvailableException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // Expiry sweep: advance 4 days, unclaimed packages return to the courier.
        clock.advance(Duration.ofDays(4));
        try {
            service.pickup(a2.lockerId(), a2.otpCode()); // expired before pickup
        } catch (InvalidOtpException e) {
            System.out.println("rejected: " + e.getMessage());
        }
        System.out.println("expired sweep returned: " + service.expireOverdue());

        // Race: two couriers, ONE free small-compatible locker — exactly one wins.
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        List<String> results = Collections.synchronizedList(new ArrayList<>());
        for (String pkgId : List.of("PKG-X", "PKG-Y")) {
            pool.submit(() -> {
                start.await();
                try {
                    results.add(pkgId + " -> " + service.assign(new Package(pkgId, Size.LARGE)).lockerId());
                } catch (NoLockerAvailableException e) {
                    results.add(pkgId + " -> rejected");
                }
                return null;
            });
        }
        start.countDown();
        pool.shutdown();
        if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("race demo did not finish");
        }
        results.stream().sorted().forEach(r -> System.out.println("race: " + r));
    }
}
