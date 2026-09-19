package problems.p06_movieticketbooking;

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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Demo {
    static final class SteppingClock extends Clock {
        private Instant now = Instant.parse("2026-10-01T18:00:00Z");

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

    /** Scriptable fake gateway: counts charges, can be told to decline the next one. */
    static final class FakeGateway implements PaymentGateway {
        final AtomicInteger charges = new AtomicInteger();
        final AtomicBoolean failNext = new AtomicBoolean(false);

        @Override
        public boolean charge(String user, long amountPaise, String idempotencyKey) {
            charges.incrementAndGet();
            return !failNext.getAndSet(false);
        }

        @Override
        public void refund(String user, long amountPaise) {
            System.out.println("[gateway] refunded ₹" + amountPaise / 100 + " to " + user);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        SteppingClock clock = new SteppingClock();
        FakeGateway gateway = new FakeGateway();
        BookingService service = new BookingService(gateway, clock, Duration.ofMinutes(10), 25_000);
        service.subscribe(b -> System.out.println("[email] confirmation for " + b));
        Show show = new Show("SHOW-1", List.of("A1", "A2", "A3", "A4", "A5"));
        service.addShow(show);

        // All-or-nothing hold of 3 adjacent seats.
        Hold alice = service.hold("SHOW-1", List.of("A1", "A2", "A3"), "alice");
        System.out.println("alice holds A1-A3: " + alice.id() + " (expires " + alice.expiresAt() + ")");

        // Partial availability: A3 is held → the WHOLE request fails, naming the blocker.
        try {
            service.hold("SHOW-1", List.of("A3", "A4"), "bob");
        } catch (SeatsUnavailableException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // Idempotent confirm: double-click retries with the same key — ONE charge, same booking.
        Booking b1 = service.confirm(alice.id(), "pay-key-alice-1");
        Booking b2 = service.confirm(alice.id(), "pay-key-alice-1");
        System.out.println("double-click: " + b1.id() + " == " + b2.id()
                + ", charges=" + gateway.charges.get());

        // Saga: payment failure releases the held seats (compensating action).
        Hold bob = service.hold("SHOW-1", List.of("A4", "A5"), "bob");
        gateway.failNext.set(true);
        try {
            service.confirm(bob.id(), "pay-key-bob-1");
        } catch (PaymentFailedException e) {
            System.out.println("rejected: " + e.getMessage());
        }
        System.out.println("A4 after failed payment: " + show.statusOf("A4"));

        // TTL expiry: a lapsed hold is reclaimed lazily by the next hold attempt.
        Hold carol = service.hold("SHOW-1", List.of("A4"), "carol");
        clock.advance(Duration.ofMinutes(11));
        Hold dave = service.hold("SHOW-1", List.of("A4"), "dave"); // reclaims carol's lapsed hold
        System.out.println("carol's hold now: " + carol.status() + "; dave holds A4: " + dave.id());
        System.out.println("sweep expired: " + service.expireHolds() + " (dave's is still live)");

        // Race: two users hold the SAME seat concurrently — exactly one wins.
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        List<String> results = Collections.synchronizedList(new ArrayList<>());
        for (String user : List.of("erin", "frank")) {
            pool.submit(() -> {
                start.await();
                try {
                    results.add(user + " -> held " + service.hold("SHOW-1", List.of("A5"), user).id());
                } catch (SeatsUnavailableException e) {
                    results.add(user + " -> rejected");
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

        // Cancellation with refund.
        service.cancelBooking(b1.id());
        System.out.println("after cancel, A1: " + show.statusOf("A1"));
    }
}
