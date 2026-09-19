package patterns.behavioral.observer;

public class SmsNotifier implements BookingObserver {
    @Override
    public void onConfirmed(String bookingId) {
        System.out.println("[sms]   confirmation sent for " + bookingId);
    }
}
