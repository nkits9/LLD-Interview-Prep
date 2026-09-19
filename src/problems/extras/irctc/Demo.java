package problems.extras.irctc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Demo {
    public static void main(String[] args) throws InterruptedException {
        TrainCatalog catalog = new TrainCatalog();
        // Route: Delhi -s0-> Agra -s1-> Bhopal -s2-> Nagpur, ONE coach, 2 seats.
        catalog.register(new Train("T101", List.of("Delhi", "Agra", "Bhopal", "Nagpur"), 2));
        catalog.register(new Train("T202", List.of("Delhi", "Jaipur"), 50));
        BookingService booking = new BookingService(catalog, new LowestNumberFirstAllocation());
        LocalDate date = LocalDate.of(2026, 10, 1);

        System.out.println("search Delhi->Nagpur : "
                + catalog.search("Delhi", "Nagpur").stream().map(Train::id).collect(Collectors.toList()));

        // THE CRUX — seat reuse across disjoint segments:
        Ticket a = booking.book("userA", "T101", date, "Delhi", "Bhopal", 1);
        System.out.println("A books Delhi->Bhopal : " + a);
        Ticket b = booking.book("userB", "T101", date, "Bhopal", "Nagpur", 1);
        System.out.println("B books Bhopal->Nagpur: " + b + "   <- SAME seat 1, disjoint segments");
        Ticket c = booking.book("userC", "T101", date, "Agra", "Nagpur", 1);
        System.out.println("C books Agra->Nagpur  : " + c);

        System.out.println("available Delhi->Nagpur: "
                + booking.availableSeats("T101", date, "Delhi", "Nagpur") + " (every seat blocked somewhere)");

        // Failure path 1: no seat free across ALL requested segments.
        try {
            booking.book("userD", "T101", date, "Delhi", "Nagpur", 1);
        } catch (NoSeatsAvailableException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // Failure path 2: direction/station validation.
        try {
            booking.book("userD", "T101", date, "Nagpur", "Delhi", 1);
        } catch (InvalidRouteException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // Cancel frees exactly A's segments (seat 1 stays booked Bhopal->Nagpur by B).
        booking.cancel(a.id());
        System.out.println("A cancelled; available Delhi->Bhopal: "
                + booking.availableSeats("T101", date, "Delhi", "Bhopal"));
        try {
            booking.cancel(a.id());
        } catch (IllegalStateException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // Concurrency: two users race for the LAST seat on Delhi->Bhopal.
        // The fair per-run lock serializes them: exactly one confirms.
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        List<String> results = Collections.synchronizedList(new ArrayList<>());
        for (String user : List.of("userX", "userY")) {
            pool.submit(() -> {
                start.await();
                try {
                    results.add(user + " -> confirmed seat " + booking.book(user, "T101", date, "Delhi", "Bhopal", 1).seatNumbers());
                } catch (NoSeatsAvailableException e) {
                    results.add(user + " -> rejected (no seats)");
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
