package problems.extras.meetingscheduler;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Demo {
    public static void main(String[] args) throws InterruptedException {
        Room huddle = new Room("huddle-4", 4);
        Room hall = new Room("hall-10", 10);
        MeetingScheduler scheduler = new MeetingScheduler(List.of(huddle, hall),
                new SmallestFitRoomSelection());

        Instant t10 = Instant.parse("2026-10-05T10:00:00Z");
        Instant t11 = Instant.parse("2026-10-05T11:00:00Z");
        Instant t1030 = Instant.parse("2026-10-05T10:30:00Z");
        Instant t1130 = Instant.parse("2026-10-05T11:30:00Z");
        Instant t12 = Instant.parse("2026-10-05T12:00:00Z");

        System.out.println("3p 10-11 : " + scheduler.book("asha", 3, t10, t11));   // smallest fit → huddle
        System.out.println("8p 10-11 : " + scheduler.book("bala", 8, t10, t11));   // needs hall

        // Overlap in every room → clean rejection.
        try {
            scheduler.book("chitra", 2, t1030, t1130);
        } catch (NoRoomAvailableException e) {
            System.out.println("rejected : " + e.getMessage());
        }

        // Back-to-back is NOT a conflict (half-open intervals).
        System.out.println("2p 11-12 : " + scheduler.book("chitra", 2, t11, t12));

        // Too many people for any room.
        try {
            scheduler.book("dev", 15, t12, Instant.parse("2026-10-05T13:00:00Z"));
        } catch (NoRoomAvailableException e) {
            System.out.println("rejected : " + e.getMessage());
        }
        // Invalid range.
        try {
            scheduler.book("dev", 2, t11, t10);
        } catch (IllegalArgumentException e) {
            System.out.println("rejected : " + e.getMessage());
        }

        // Cancel frees the slot for rebooking.
        Booking mtg = scheduler.book("esha", 2, t12, Instant.parse("2026-10-05T13:00:00Z"));
        scheduler.cancel(mtg.id());
        System.out.println("rebooked : " + scheduler.book("farah", 2, t12,
                Instant.parse("2026-10-05T13:00:00Z")));

        // Race: two organizers want the ONLY free slot in the only fitting room.
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        List<String> results = Collections.synchronizedList(new ArrayList<>());
        Instant t14 = Instant.parse("2026-10-05T14:00:00Z");
        Instant t15 = Instant.parse("2026-10-05T15:00:00Z");
        for (String who : List.of("gita", "hari")) {
            pool.submit(() -> {
                start.await();
                try {
                    results.add(who + " -> " + scheduler.book(who, 8, t14, t15).roomId());
                } catch (NoRoomAvailableException e) {
                    results.add(who + " -> rejected");
                }
                return null;
            });
        }
        start.countDown();
        pool.shutdown();
        if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("race demo did not finish");
        }
        results.stream().sorted().forEach(r -> System.out.println("race     : " + r));
    }
}
