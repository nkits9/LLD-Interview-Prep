package problems.p03_parkinglot;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    private static String rupees(long paise) {
        return String.format("₹%d.%02d", paise / 100, paise % 100);
    }

    public static void main(String[] args) throws InterruptedException {
        SteppingClock clock = new SteppingClock();
        ParkingLotService lot = new ParkingLotService(
                List.of(new Spot("S-1", SpotType.SMALL), new Spot("M-1", SpotType.MEDIUM),
                        new Spot("L-1", SpotType.LARGE)),
                new TightestFitAllocation(),
                new HourlyPricing(Map.of(VehicleType.BIKE, 2_000L, VehicleType.CAR, 5_000L,
                        VehicleType.TRUCK, 10_000L)),
                clock,
                50_000L); // lost-ticket penalty ₹500

        // Tightest fit: bike takes SMALL, not MEDIUM/LARGE.
        Ticket bike = lot.park(Vehicle.of(VehicleType.BIKE, "KA-01-1111"));
        System.out.println("parked: " + bike);
        Ticket car = lot.park(Vehicle.of(VehicleType.CAR, "KA-02-2222"));
        System.out.println("parked: " + car + "   <- car skips SMALL (matrix), takes MEDIUM");
        Ticket truck = lot.park(Vehicle.of(VehicleType.TRUCK, "KA-03-3333"));
        System.out.println("parked: " + truck);

        // Failure: lot full for this type.
        try {
            lot.park(Vehicle.of(VehicleType.CAR, "KA-04-4444"));
        } catch (LotFullException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // Time-based fee with round-up: 2h30m → 3 started hours.
        clock.advance(Duration.ofMinutes(150));
        System.out.println("car exit fee (2h30m @ ₹50/h): " + rupees(lot.unpark(car.id())));

        // Failure: a used ticket cannot exit twice.
        try {
            lot.unpark(car.id());
        } catch (InvalidTicketException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // Lost ticket → flat penalty, spot still freed.
        System.out.println("truck lost ticket penalty : " + rupees(lot.exitWithLostTicket("KA-03-3333")));

        // Race: two cars, one free MEDIUM spot (car freed it) + LARGE (truck freed it) = 2...
        // park a car to leave exactly ONE car-compatible spot, then race for it.
        lot.park(Vehicle.of(VehicleType.CAR, "KA-05-5555"));
        System.out.println("free car-compatible spots: " + lot.freeSpotCount(VehicleType.CAR));
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        List<String> results = Collections.synchronizedList(new ArrayList<>());
        for (String plate : List.of("KA-06-6666", "KA-07-7777")) {
            pool.submit(() -> {
                start.await();
                try {
                    results.add(plate + " -> " + lot.park(Vehicle.of(VehicleType.CAR, plate)).spotId());
                } catch (LotFullException e) {
                    results.add(plate + " -> rejected");
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
