package problems.extras.hotelbooking;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Demo {
    public static void main(String[] args) throws InterruptedException {
        Hotel hotel = new Hotel();
        hotel.addRooms(RoomType.STANDARD, 2);

        LocalDate oct1 = LocalDate.of(2026, 10, 1);
        LocalDate oct3 = LocalDate.of(2026, 10, 3);
        LocalDate oct4 = LocalDate.of(2026, 10, 4);
        LocalDate oct6 = LocalDate.of(2026, 10, 6);

        // Overlapping ranges share per-night counters.
        Booking g1 = hotel.book("g1", RoomType.STANDARD, oct1, oct4, 1); // nights 1,2,3
        Booking g2 = hotel.book("g2", RoomType.STANDARD, oct3, oct6, 1); // nights 3,4,5
        System.out.println(g1 + "\n" + g2);
        System.out.println("available Oct 3 night: " + hotel.available(RoomType.STANDARD, oct3));

        // Night 3 is full → an overlapping request fails ALL-OR-NOTHING.
        try {
            hotel.book("g3", RoomType.STANDARD, oct3, oct6, 1);
        } catch (NoAvailabilityException e) {
            System.out.println("rejected: " + e.getMessage());
        }
        // But checking in AFTER the crunch night works (checkout day frees the night).
        System.out.println(hotel.book("g3", RoomType.STANDARD, oct4, oct6, 1));

        // Cancel frees every night of the stay; the crunch night reopens.
        hotel.cancel(g1.id());
        System.out.println("after g1 cancel, Oct 3 night: "
                + hotel.available(RoomType.STANDARD, oct3));
        try {
            hotel.cancel(g1.id());
        } catch (IllegalStateException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // Invalid range and unknown type.
        try {
            hotel.book("g4", RoomType.STANDARD, oct4, oct4, 1);
        } catch (IllegalArgumentException e) {
            System.out.println("rejected: " + e.getMessage());
        }
        try {
            hotel.book("g4", RoomType.SUITE, oct1, oct3, 1);
        } catch (IllegalArgumentException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // Race: ONE standard room left on Oct 3 night — two guests want it.
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        List<String> results = Collections.synchronizedList(new ArrayList<>());
        for (String guest : List.of("gx", "gy")) {
            pool.submit(() -> {
                start.await();
                try {
                    results.add(guest + " -> " + hotel.book(guest, RoomType.STANDARD, oct3, oct4, 1).id());
                } catch (NoAvailabilityException e) {
                    results.add(guest + " -> rejected");
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
