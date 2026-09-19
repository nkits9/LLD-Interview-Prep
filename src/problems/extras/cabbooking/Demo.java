package problems.extras.cabbooking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Demo {
    public static void main(String[] args) throws InterruptedException {
        // Surge: 1.0x while supply covers demand, 1.5x tight, 2.0x when demand >= 2x supply.
        SurgeStrategy thresholds = (active, available) -> {
            if (available == 0 || active >= 2 * available) {
                return 20_000;
            }
            return active >= available ? 15_000 : 10_000;
        };
        // Fare: ₹50 base + ₹12/km, times surge.
        FareStrategy fare = (km, surgeBp) -> (5_000 + 1_200L * km) * surgeBp / 10_000;
        RideService service = new RideService(new NearestDriverMatching(), thresholds, fare);
        service.register(new Driver("d1", new Location(0, 0)));
        service.register(new Driver("d2", new Location(5, 5)));
        service.register(new Driver("d3", new Location(10, 10)));

        // Nearest matching + surge locked at request.
        Trip t1 = service.requestRide("asha", new Location(1, 1), new Location(9, 9));
        System.out.println("t1: " + t1 + "   <- nearest driver d1");
        Trip t2 = service.requestRide("bala", new Location(6, 6), new Location(0, 0));
        System.out.println("t2: " + t2);
        Trip t3 = service.requestRide("chitra", new Location(9, 9), new Location(2, 2));
        System.out.println("t3: " + t3 + "   <- demand 2, supply 1: surge kicked in");

        // Same distance, different surge → different fare.
        service.startTrip(t1.id());
        service.startTrip(t3.id());
        long fare1 = service.completeTrip(t1.id(), 16);
        long fare3 = service.completeTrip(t3.id(), 16);
        System.out.println("16km at 100%: ₹" + fare1 / 100 + " | 16km at "
                + t3.surgeBasisPoints() / 100 + "%: ₹" + fare3 / 100);

        // Cancellation rules come from the transition table.
        service.cancelTrip(t2.id());
        System.out.println("t2 cancelled before start: " + t2.status());
        try {
            Trip t4 = service.requestRide("dev", new Location(3, 3), new Location(8, 8));
            service.startTrip(t4.id());
            service.cancelTrip(t4.id()); // ONGOING → CANCELLED not allowed
        } catch (IllegalStateException e) {
            System.out.println("rejected: " + e.getMessage());
        }
        try {
            Trip t5 = service.requestRide("esha", new Location(2, 2), new Location(4, 4));
            service.completeTrip(t5.id(), 4); // must start first
        } catch (IllegalStateException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // Race: one free driver, two simultaneous requests — exactly one matched.
        RideService raceService = new RideService(new NearestDriverMatching(), thresholds, fare);
        raceService.register(new Driver("solo", new Location(0, 0)));
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        List<String> results = Collections.synchronizedList(new ArrayList<>());
        for (String rider : List.of("r1", "r2")) {
            pool.submit(() -> {
                start.await();
                try {
                    results.add(rider + " -> " + raceService
                            .requestRide(rider, new Location(1, 0), new Location(5, 5)).driverId());
                } catch (NoDriverAvailableException e) {
                    results.add(rider + " -> rejected");
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
