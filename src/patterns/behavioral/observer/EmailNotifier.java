package patterns.behavioral.observer;

public class EmailNotifier implements BookingObserver {
    @Override
    public void onConfirmed(String bookingId) {
        System.out.println("[email] confirmation sent for " + bookingId);
    }
}
