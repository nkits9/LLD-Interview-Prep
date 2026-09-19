package patterns.behavioral.observer;

/** Observers only know the event, never the service internals. */
@FunctionalInterface
public interface BookingObserver {
    void onConfirmed(String bookingId);
}
