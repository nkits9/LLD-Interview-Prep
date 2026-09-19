package patterns.creational.factory;

public class Demo {
    public static void main(String[] args) {
        NotificationFactory factory = new NotificationFactory();
        factory.register(ChannelType.EMAIL, EmailNotification::new);
        factory.register(ChannelType.SMS, SmsNotification::new);

        factory.create(ChannelType.EMAIL).send("ankit", "Booking BK-1 confirmed");
        factory.create(ChannelType.SMS).send("ankit", "Booking BK-1 confirmed");

        // Failure path: unknown type is a specific exception, never null.
        try {
            factory.create(ChannelType.PUSH);
        } catch (UnsupportedChannelException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // Extension: a new channel registers itself — zero factory edits.
        factory.register(ChannelType.PUSH,
                () -> (to, message) -> System.out.println("[push]  to " + to + ": " + message));
        factory.create(ChannelType.PUSH).send("ankit", "Booking BK-1 confirmed");
    }
}
