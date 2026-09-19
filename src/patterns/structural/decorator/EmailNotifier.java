package patterns.structural.decorator;

/** The concrete component — the real work every wrapper eventually reaches. */
public class EmailNotifier implements Notifier {
    @Override
    public void send(String message) {
        System.out.println("[email] " + message);
    }
}
