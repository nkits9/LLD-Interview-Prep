package problems.p11_fooddelivery;

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

public class Demo {
    static final class SteppingClock extends Clock {
        private Instant now = Instant.parse("2026-10-01T13:00:00Z");

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
        OrderService service = new OrderService(new NearestRiderMatching(),
                distance -> 2_000 + 500L * distance,   // fee: ₹20 base + ₹5/km
                clock, Duration.ofMinutes(15), 5_000); // 15-min accept timeout, ₹50 late-cancel fee
        // Observer: each persona reacts differently to the same event.
        service.subscribe((o, from, to) -> System.out.println(
                "[customer] " + o.customer() + ": order " + o.id() + " is now " + to));
        service.subscribe((o, from, to) -> {
            if (to == OrderStatus.PLACED || to == OrderStatus.CANCELLED) {
                System.out.println("[restaurant] " + o.restaurantId() + ": " + o.id() + " " + to);
            }
        });

        Restaurant biryaniHouse = new Restaurant("biryani-house", 5, 2); // capacity 2
        service.register(biryaniHouse);
        service.register(new Rider("rider-1", 3));
        service.register(new Rider("rider-2", 8));

        // Happy path with nearest-rider matching and distance fee.
        Order o1 = service.place("asha", "biryani-house", 9, 30_000);
        System.out.println("fee for 4km: ₹" + o1.deliveryFeePaise() / 100);
        service.accept(o1.id());
        service.startPreparing(o1.id());

        // Illegal transition rejected explicitly.
        try {
            service.deliver(o1.id());
        } catch (IllegalTransitionException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // THE race: two riders accept the same order — CAS lets exactly one win.
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        List<String> results = Collections.synchronizedList(new ArrayList<>());
        for (String riderId : List.of("rider-1", "rider-2")) {
            pool.submit(() -> {
                start.await();
                results.add(riderId + " -> " + (service.riderAccepts(o1.id(), riderId) ? "WON" : "lost"));
                return null;
            });
        }
        start.countDown();
        pool.shutdown();
        if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("race demo did not finish");
        }
        results.stream().sorted().forEach(r -> System.out.println("race: " + r));
        String winner = o1.rider();

        // Rider cancels -> automatic reassignment to the next candidate.
        String replacement = service.riderCancels(o1.id(), winner);
        System.out.println(winner + " cancelled; reassigned to " + replacement);

        service.pickUp(o1.id());
        service.deliver(o1.id());

        // Capacity: two kitchens slots; third concurrent order is refused.
        Order o2 = service.place("bala", "biryani-house", 2, 20_000);
        Order o3 = service.place("chitra", "biryani-house", 3, 15_000);
        Order o4 = service.place("dev", "biryani-house", 4, 18_000);
        service.accept(o2.id());
        service.accept(o3.id());
        try {
            service.accept(o4.id());
        } catch (RestaurantAtCapacityException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // Cancellation rules: free before accept, charged after, impossible after pickup.
        service.cancel(o4.id());                       // PLACED -> free
        service.cancel(o2.id());                       // ACCEPTED -> ₹50 fee, slot released
        try {
            service.cancel(o1.id());                   // DELIVERED -> not allowed
        } catch (CancellationNotAllowedException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // Timeout: restaurant never accepts o5 -> sweep auto-cancels with full refund.
        Order o5 = service.place("esha", "biryani-house", 6, 12_000);
        clock.advance(Duration.ofMinutes(16));
        System.out.println("stale sweep cancelled: " + service.cancelStaleOrders()
                + " (order " + o5.id() + " is " + o5.status() + ")");
    }
}
