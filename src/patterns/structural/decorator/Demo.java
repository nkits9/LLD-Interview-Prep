package patterns.structural.decorator;

public class Demo {
    public static void main(String[] args) {
        // A flaky component: fails twice, then succeeds.
        Notifier flaky = new Notifier() {
            private int calls = 0;

            @Override
            public void send(String message) {
                if (++calls < 3) {
                    throw new IllegalStateException("SMTP timeout");
                }
                System.out.println("[email] " + message);
            }
        };

        // Stacking order matters: log OUTSIDE retry → one log line around all attempts.
        Notifier notifier = new LoggingNotifier(new RetryingNotifier(flaky, 3));
        notifier.send("Booking BK-1 confirmed");

        System.out.println("--");

        // Plain component, same client code — decorators are invisible to callers.
        new LoggingNotifier(new EmailNotifier()).send("Booking BK-2 confirmed");

        System.out.println("--");

        // Failure path: retries exhausted → the failure propagates.
        Notifier alwaysDown = message -> {
            throw new IllegalStateException("SMTP down");
        };
        try {
            new RetryingNotifier(alwaysDown, 2).send("Booking BK-3 confirmed");
        } catch (IllegalStateException e) {
            System.out.println("gave up after retries: " + e.getMessage());
        }
    }
}
