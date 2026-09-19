package problems.extras.librarymanagement;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class Demo {
    static final class SteppingClock extends Clock {
        private Instant now = Instant.parse("2026-10-01T10:00:00Z");

        void advanceDays(int days) {
            now = now.plus(Duration.ofDays(days));
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

    public static void main(String[] args) {
        SteppingClock clock = new SteppingClock();
        LibraryService library = new LibraryService(clock);
        library.addBook(new Book("978-0134685991", "Effective Java"), 2);
        library.addBook(new Book("978-0321349606", "JCiP"), 1);
        library.addBook(new Book("978-1449373320", "DDIA"), 2);

        Loan l1 = library.checkout("m1", "978-0134685991");
        Loan l2 = library.checkout("m2", "978-0134685991");
        System.out.println("loans: " + l1 + " | " + l2);

        // All copies out → explicit failure; m3 reserves instead.
        try {
            library.checkout("m3", "978-0134685991");
        } catch (NoCopyAvailableException e) {
            System.out.println("rejected: " + e.getMessage());
        }
        library.reserve("m3", "978-0134685991");

        // Late return: due in 14 days, returned on day 20 → 6 days × ₹10 fine,
        // and the freed copy goes straight to m3's reservation, not the shelf.
        clock.advanceDays(20);
        long fine = library.returnCopy(l1.copyId());
        System.out.println("m1 returned 6 days late, fine ₹" + fine / 100
                + "; EJ available: " + library.availableCopies("978-0134685991")
                + " (reservation grabbed it)");

        // Borrow limit: m2 holds EJ; JCiP and DDIA make 3; the 4th is refused
        // even though a DDIA copy sits free on the shelf.
        library.checkout("m2", "978-0321349606");
        library.checkout("m2", "978-1449373320");
        try {
            library.checkout("m2", "978-1449373320");
        } catch (BorrowLimitExceededException e) {
            System.out.println("rejected: " + e.getMessage()
                    + " (DDIA available: " + library.availableCopies("978-1449373320") + ")");
        }

        // Returning something not on loan.
        try {
            library.returnCopy("bogus#99");
        } catch (IllegalArgumentException e) {
            System.out.println("rejected: " + e.getMessage());
        }
    }
}
