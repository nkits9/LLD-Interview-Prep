package patterns.structural.decorator;

/** Adds logging around ANY notifier. */
public class LoggingNotifier implements Notifier {
    private final Notifier delegate;

    public LoggingNotifier(Notifier delegate) {
        this.delegate = delegate;
    }

    @Override
    public void send(String message) {
        System.out.println("[log]   sending: " + message);
        delegate.send(message);
        System.out.println("[log]   sent ok");
    }
}
