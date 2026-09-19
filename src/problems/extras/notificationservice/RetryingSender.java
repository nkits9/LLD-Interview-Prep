package problems.extras.notificationservice;

/** Decorator: adds retry-with-backoff around ANY sender; the wrapped one never knows. */
public class RetryingSender implements ChannelSender {
    private final ChannelSender delegate;
    private final RetryPolicy policy;
    private final Sleeper sleeper;

    public RetryingSender(ChannelSender delegate, RetryPolicy policy, Sleeper sleeper) {
        this.delegate = delegate;
        this.policy = policy;
        this.sleeper = sleeper;
    }

    @Override
    public void send(Notification notification) throws Exception {
        for (int attempt = 1; ; attempt++) {
            try {
                delegate.send(notification);
                return;
            } catch (Exception e) {
                if (attempt == policy.maxAttempts()) {
                    throw new Exception("gave up after " + attempt + " attempts: " + e.getMessage(), e);
                }
                sleeper.sleep(policy.delayBeforeAttempt(attempt + 1)); // backoff, never busy-spin
            }
        }
    }
}
