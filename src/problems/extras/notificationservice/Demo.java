package problems.extras.notificationservice;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Demo {
    public static void main(String[] args) throws InterruptedException {
        // Recorded sleeper: backoff is observable without actually sleeping (testability).
        List<String> recordedDelays = new java.util.concurrent.CopyOnWriteArrayList<>();
        Sleeper recordingSleeper = d -> recordedDelays.add(d.toMillis() + "ms");
        RetryPolicy threeAttempts = new RetryPolicy(3, Duration.ofMillis(100));

        SenderRegistry registry = new SenderRegistry();
        registry.register(ChannelType.EMAIL,
                n -> System.out.println("[email] to " + n.userId() + ": " + n.message()));

        // Flaky SMS: fails twice, then delivers — healed by the retry decorator.
        AtomicInteger smsCalls = new AtomicInteger();
        ChannelSender flakySms = n -> {
            if (smsCalls.incrementAndGet() < 3) {
                throw new IllegalStateException("SMS gateway timeout");
            }
            System.out.println("[sms]   to " + n.userId() + ": " + n.message());
        };
        registry.register(ChannelType.SMS, new RetryingSender(flakySms, threeAttempts, recordingSleeper));

        // Dead PUSH: always fails — retries exhaust, result reports it, others unaffected.
        ChannelSender deadPush = n -> {
            throw new IllegalStateException("push token expired");
        };
        registry.register(ChannelType.PUSH, new RetryingSender(deadPush, threeAttempts, recordingSleeper));

        ExecutorService pool = Executors.newFixedThreadPool(3);
        NotificationService service = new NotificationService(registry, pool);
        service.setPreferences("ankit", Set.of(ChannelType.EMAIL, ChannelType.SMS, ChannelType.PUSH));

        List<DeliveryResult> results = service.notify("ankit", "Booking BK-42 confirmed");
        results.forEach(r -> System.out.println("result  " + r));
        System.out.println("backoff delays used: " + recordedDelays);

        // A user with no preferences gets nothing — not an error.
        System.out.println("no prefs -> " + service.notify("ghost", "hello").size() + " deliveries");

        pool.shutdown();
        pool.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
    }
}
