package patterns.behavioral.observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Subject. Knows the observer interface, never who listens — adding a new
 * notification channel touches nothing here (OCP).
 */
public class BookingService {
    // CopyOnWriteArrayList: iteration is safe while others (un)subscribe —
    // the read-heavy/rare-write case where copy-on-write shines.
    private final List<BookingObserver> observers = new CopyOnWriteArrayList<>();

    public void subscribe(BookingObserver observer) {
        observers.add(observer);
    }

    public void unsubscribe(BookingObserver observer) {
        observers.remove(observer);
    }

    public void confirmBooking(String bookingId) {
        // Mutate own state first, notify after — observers see committed truth.
        System.out.println("[booking] " + bookingId + " confirmed");
        for (BookingObserver observer : observers) {
            try {
                observer.onConfirmed(bookingId);
            } catch (RuntimeException e) {
                // A failing observer must not break the booking or the other observers.
                System.out.println("[booking] observer failed, continuing: " + e.getMessage());
            }
        }
    }
}
