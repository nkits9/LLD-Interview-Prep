package patterns.behavioral.observer;

public class Demo {
    public static void main(String[] args) {
        BookingService service = new BookingService();
        SmsNotifier sms = new SmsNotifier();

        service.subscribe(new EmailNotifier());
        service.subscribe(sms);
        // Failure path: a broken observer is isolated, others still notified.
        service.subscribe(bookingId -> {
            throw new IllegalStateException("analytics sink is down");
        });

        service.confirmBooking("BK-1");

        System.out.println("-- sms unsubscribed --");
        service.unsubscribe(sms);
        service.confirmBooking("BK-2");
    }
}
