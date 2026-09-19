package problems.p06_movieticketbooking;

/** Email/SMS on confirm — notified OUTSIDE all locks. */
@FunctionalInterface
public interface BookingObserver {
    void onConfirmed(Booking booking);
}
