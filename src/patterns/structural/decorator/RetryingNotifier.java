package patterns.structural.decorator;

/** Adds retry around ANY notifier — the wrapped one never knows. */
public class RetryingNotifier implements Notifier {
    private final Notifier delegate;
    private final int maxAttempts;

    public RetryingNotifier(Notifier delegate, int maxAttempts) {
        this.delegate = delegate;
        this.maxAttempts = maxAttempts;
    }

    @Override
    public void send(String message) {
        for (int attempt = 1; ; attempt++) {
            try {
                delegate.send(message);
                return;
            } catch (RuntimeException e) {
                if (attempt == maxAttempts) {
                    throw e; // exhausted — propagate, caller decides
                }
                System.out.println("[retry] attempt " + attempt + " failed (" + e.getMessage() + "), retrying");
            }
        }
    }
}
